package com.ecommerce.simple.controller;

import com.ecommerce.simple.exception.CustomExceptionHandler;
import com.ecommerce.simple.model.Order;
import com.ecommerce.simple.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.ConnectException;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>(DockerImageName.parse("postgres:latest"));

    private static String asJsonString(Object object) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 200
     */
    @Test
    @org.junit.jupiter.api.Order(1)
    public void createOrder() throws Exception {
        var order = Order.builder()
                .description("sales 1")
                .totalAmount(0.0)
                .build();
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/orders")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(order)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("sales 1"))
                .andExpect(jsonPath("$.totalAmount").value(0));
    }

    /**
     * 200
     */
    @Test
    @org.junit.jupiter.api.Order(2)
    public void getAllOrders() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/orders")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").isArray());
    }

    /**
     * 200
     */
    @Test
    @org.junit.jupiter.api.Order(4)
    public void getOrderById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/orders/1")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("sales 1"))
                .andExpect(jsonPath("$.totalAmount").value(0));
    }

    /**
     * 200
     */
    @Test
    @org.junit.jupiter.api.Order(5)
    public void updateOrder() throws Exception {
        var order = Order.builder()
                .description("sales 2")
                .totalAmount(1000.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/orders/1")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(order)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("sales 2"))
                .andExpect(jsonPath("$.totalAmount").value(1000.0));
    }

    /**
     * 200
     */
    @Test
    @org.junit.jupiter.api.Order(6)
    public void updateOrderTryingToChangeAnotherOrder() throws Exception {
        var order1 = Order.builder()
                .description("sales 10")
                .totalAmount(0.0)
                .build();
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/orders")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(order1)));

        var order = Order.builder()
                .id(2)
                .description("sales 2")
                .totalAmount(1000.0)
                .build();
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/orders/1")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(order)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("sales 2"))
                .andExpect(jsonPath("$.totalAmount").value(1000.0));

        // second not altered
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/orders/2")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.description").value("sales 10"))
                .andExpect(jsonPath("$.totalAmount").value(0));
    }

    /**
     * 200
     */
    @Test
    @org.junit.jupiter.api.Order(7)
    public void deleteOrder() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/orders/2"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * 400
     */
    @Test
    @org.junit.jupiter.api.Order(8)
    public void createOrderWithoutDescription() throws Exception {
        var order = Order.builder()
                .totalAmount(0.0)
                .build();
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/orders")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(order)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[description is mandatory]"));
    }

    /**
     * 400
     */
    @Test
    @org.junit.jupiter.api.Order(8)
    public void createOrderWithoutAmount() throws Exception {
        var order = Order.builder()
                .description("sales 2")
                .build();
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/orders")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(order)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[totalAmount is mandatory]"));
    }

    /**
     * 400
     */
    @Test
    @org.junit.jupiter.api.Order(9)
    public void createOrderWithoutBody() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/orders")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(""))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("Required request body is missing: "));
    }

    /**
     * 400
     */
    @Test
    @org.junit.jupiter.api.Order(10)
    public void createOrderWithEmptyBody() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/orders")
                        .contentType("application/json")
                        .accept("application/json")
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[description is mandatory, totalAmount is mandatory]"));
    }

    /**
     * 404
     */
    @Test
    @org.junit.jupiter.api.Order(11)
    public void deleteOrderNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/orders/2"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Order of id 2 not found."));
    }

    /**
     * 404
     */
    @Test
    @org.junit.jupiter.api.Order(12)
    public void getAllOrdersNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/orders/1"));
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/orders/3"));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/orders")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("No orders found."));
    }

    /**
     * 404
     */
    @Test
    @org.junit.jupiter.api.Order(13)
    public void getOrderByIdNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/orders/100")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Order of id 100 not found."));
    }

    /**
     * 404
     */
    @Test
    @org.junit.jupiter.api.Order(14)
    public void updateOrderWithIdNotFound() throws Exception {
        var order = Order.builder()
                .description("sales 2")
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/orders/100")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(order)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Order of id 100 not found."));
    }

    /**
     * 404
     */
    @Test
    @org.junit.jupiter.api.Order(15)
    public void deleteOrderWithIdNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/orders/100"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Order of id 100 not found."));
    }

    /**
     * 405
     */
    @Test
    @org.junit.jupiter.api.Order(16)
    public void methodNoAllowed() throws Exception {

        var order = Order.builder()
                .description("sales 1")
                .build();
        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/api/orders")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(order)))
                .andDo(print())
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.httpCode").value(405))
                .andExpect(jsonPath("$.message").value("Method Not Allowed"))
                .andExpect(jsonPath("$.detailedMessage").value("Request method 'PATCH' is not supported"));
    }

    /**
     * 406
     */
    @Test
    @org.junit.jupiter.api.Order(17)
    public void acceptNotSet() throws Exception {

        var order = Order.builder()
                .description("sales 1")
                .totalAmount(0.0)
                .build();
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/orders")
                        .contentType("application/json")
                        .content(asJsonString(order)))
                .andDo(print())
                .andExpect(status().isNotAcceptable())
                .andExpect(jsonPath("$.httpCode").value(406))
                .andExpect(jsonPath("$.message").value("Not Acceptable"))
                .andExpect(jsonPath("$.detailedMessage").value("No converter with preset Content-Type 'null'"));
    }

    /**
     * 415
     */
    @Test
    @org.junit.jupiter.api.Order(18)
    public void contentTypedNotSupported() throws Exception {

        var order = Order.builder()
                .description("sales 1")
                .build();
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/orders")
                        .contentType("application/json2")
                        .accept("application/json")
                        .content(asJsonString(order)))
                .andDo(print())
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.httpCode").value(415))
                .andExpect(jsonPath("$.message").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.detailedMessage").value("Content-Type 'application/json2' is not supported"));
    }

    /**
     * 503
     */
    @Test
    @org.junit.jupiter.api.Order(19)
    public void notMappedException() throws Exception {
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        OrderController orderController = new OrderController(orderRepository);

        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();

        given(orderRepository.findAll())
                .willAnswer(invocation -> {
                    throw new RuntimeException("Generic error.");
                });

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/orders")
                        .contentType("application/json")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.httpCode").value(500))
                .andExpect(jsonPath("$.message").value("Internal Server Error"))
                .andExpect(jsonPath("$.detailedMessage").value("Generic error."));
    }

    /**
     * 503
     */
    @Test
    @org.junit.jupiter.api.Order(20)
    public void serviceUnavailable() throws Exception {
        OrderRepository orderRepository = Mockito.mock(OrderRepository.class);
        OrderController orderController = new OrderController(orderRepository);

        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();

        given(orderRepository.findAll())
                .willAnswer(invocation -> {
                    throw new ConnectException("Connection error.");
                });

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/orders")
                        .contentType("application/json")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.httpCode").value(503))
                .andExpect(jsonPath("$.message").value("Service Unavailable"))
                .andExpect(jsonPath("$.detailedMessage").value("Connection error."));
    }
}