package com.ecommerce.simple.controller;

import com.ecommerce.simple.dto.OrderItemRequestDTO;
import com.ecommerce.simple.dto.OrderResponseDTO;
import com.ecommerce.simple.dto.ProductResponseDTO;
import com.ecommerce.simple.exception.CustomExceptionHandler;
import com.ecommerce.simple.model.Order;
import com.ecommerce.simple.model.Product;
import com.ecommerce.simple.service.OrderItemService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
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

import static java.lang.String.format;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderItemControllerTest {

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
    public void createOrderItem() throws Exception {
        Integer orderId = createOrder("sales 1");
        Integer productId = createProduct("new tv", 10);

        var orderItem = OrderItemRequestDTO.builder()
                .orderId(orderId)
                .productId(productId)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/order-items")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(orderItem)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("new tv"))
                .andExpect(jsonPath("$.productDescription").value("high definition television"))
                .andExpect(jsonPath("$.productPrice").value(500.0));

        checkingProductLeftOver(productId);
        checkingOrderTotalAmount(orderId);
    }

    /**
     * 200
     */
    @Test
    @org.junit.jupiter.api.Order(2)
    public void getAllOrderItems() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/order-items")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].orderId").value(1))
                .andExpect(jsonPath("$[0].productId").value(1))
                .andExpect(jsonPath("$[0].productName").value("new tv"))
                .andExpect(jsonPath("$[0].productDescription").value("high definition television"))
                .andExpect(jsonPath("$[0].productPrice").value(500.0));
    }

    /**
     * 200
     */
    @Test
    @org.junit.jupiter.api.Order(3)
    public void getOrderItemById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/order-items/1")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(1))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.productName").value("new tv"))
                .andExpect(jsonPath("$.productDescription").value("high definition television"))
                .andExpect(jsonPath("$.productPrice").value(500.0));
    }

    /**
     * 200
     */
    @Test
    @org.junit.jupiter.api.Order(4)
    public void updateOrderItem_removingItemFromOrder() throws Exception {
        Integer orderId = 1;
        Integer productId = createProduct2();

        var orderItem = OrderItemRequestDTO.builder()
                .orderId(orderId)
                .productId(productId)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/order-items/1")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(orderItem)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.orderId").value(orderId))
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.productName").value("pc"))
                .andExpect(jsonPath("$.productDescription").value("personal computer"))
                .andExpect(jsonPath("$.productPrice").value(1000.0));

        checkingProductLeftOverUpdate(productId);
        checkingProductAbandoned(1);
        checkingOrderTotalAmountUpdate(orderId);
    }

    /**
     * 200
     */
    @Test
    @org.junit.jupiter.api.Order(5)
    public void deleteOrderItem() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/order-items/1"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * 400
     */
    @Test
    @org.junit.jupiter.api.Order(6)
    public void createSameOrderTwice() throws Exception {
        Integer orderId = createOrder("sales 2");
        Integer productId = createProduct("new tv 2", 10);

        // first time
        var orderItem = OrderItemRequestDTO.builder()
                .orderId(orderId)
                .productId(productId)
                .build();
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/order-items")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(orderItem)));

        // second time
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/order-items")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(orderItem)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value(format("Order item already exists for order: '%d' and product: '%d'.", orderId, productId)));
    }

    /**
     * 400
     */
    @Test
    @org.junit.jupiter.api.Order(7)
    public void createOrderWithNoProductQuantity() throws Exception {
        Integer orderId = createOrder("sales 3");
        Integer productId = createProduct("new tv 3", 0);

        var orderItem = OrderItemRequestDTO.builder()
                .orderId(orderId)
                .productId(productId)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/order-items")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(orderItem)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value(format("There is no left over products, quantity: %d", 0)));
    }

    /**
     * 400
     */
    @Test
    @org.junit.jupiter.api.Order(8)
    public void createOrderItemWithoutOrderId() throws Exception {
        var orderItem = OrderItemRequestDTO.builder()
                .productId(1)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/order-items")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(orderItem)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[orderId is mandatory]"));
    }

    /**
     * 400
     */
    @Test
    @org.junit.jupiter.api.Order(9)
    public void createOrderItemWithoutProductId() throws Exception {
        var orderItem = OrderItemRequestDTO.builder()
                .orderId(1)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/order-items")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(orderItem)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[productId is mandatory]"));
    }

    /**
     * 400
     */
    @Test
    @org.junit.jupiter.api.Order(10)
    public void createOrderItemWithoutBody() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/order-items")
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
    @org.junit.jupiter.api.Order(11)
    public void createOrderItemWithEmptyBody() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/order-items")
                        .contentType("application/json")
                        .accept("application/json")
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[orderId is mandatory, productId is mandatory]"));
    }

    /**
     * 404
     */
    @Test
    @org.junit.jupiter.api.Order(12)
    public void deleteOrderItemNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/order-items/10"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Order item of id 10 not found."));
    }

    /**
     * 404
     */
    @Test
    @org.junit.jupiter.api.Order(13)
    public void getAllOrderItemsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/order-items/1"));
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/order-items/2"));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/order-items")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("No order items found."));
    }

    /**
     * 404
     */
    @Test
    @org.junit.jupiter.api.Order(14)
    public void getOrderItemByIdNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/order-items/100")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Order item of id 100 not found."));
    }

    /**
     * 404
     */
    @Test
    @org.junit.jupiter.api.Order(15)
    public void updateOrderItemWithIdNotFound() throws Exception {
        var orderItem = OrderItemRequestDTO.builder()
                .orderId(1)
                .productId(1)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/order-items/100")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(orderItem)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Order item of id 100 not found."));
    }

    /**
     * 404
     */
    @Test
    @org.junit.jupiter.api.Order(16)
    public void deleteOrderItemWithIdNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/order-items/100"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Order item of id 100 not found."));
    }

    /**
     * 405
     */
    @Test
    @org.junit.jupiter.api.Order(17)
    public void methodNoAllowed() throws Exception {
        var orderItem = OrderItemRequestDTO.builder()
                .orderId(1)
                .productId(1)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/api/order-items")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(orderItem)))
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
    @org.junit.jupiter.api.Order(18)
    public void acceptNotSet() throws Exception {
        var orderItem = OrderItemRequestDTO.builder()
                .orderId(1)
                .productId(1)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/order-items")
                        .contentType("application/json")
                        .content(asJsonString(orderItem)))
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
    @org.junit.jupiter.api.Order(19)
    public void contentTypedNotSupported() throws Exception {
        var orderItem = OrderItemRequestDTO.builder()
                .orderId(1)
                .productId(1)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/order-items")
                        .contentType("application/json2")
                        .accept("application/json")
                        .content(asJsonString(orderItem)))
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
    @org.junit.jupiter.api.Order(20)
    public void notMappedException() throws Exception {
        OrderItemService orderService = Mockito.mock(OrderItemService.class);
        OrderItemController orderController = new OrderItemController(orderService);

        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();

        given(orderService.getOrderItems())
                .willAnswer(invocation -> {
                    throw new RuntimeException("Generic error.");
                });

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/order-items")
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
    @org.junit.jupiter.api.Order(21)
    public void serviceUnavailable() throws Exception {
        OrderItemService orderService = Mockito.mock(OrderItemService.class);
        OrderItemController orderController = new OrderItemController(orderService);

        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();

        given(orderService.getOrderItems())
                .willAnswer(invocation -> {
                    throw new ConnectException("Connection error.");
                });

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/order-items")
                        .contentType("application/json")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.httpCode").value(503))
                .andExpect(jsonPath("$.message").value("Service Unavailable"))
                .andExpect(jsonPath("$.detailedMessage").value("Connection error."));
    }

    private Integer createProduct(String name, int quantity) throws Exception {
        var product = Product.builder()
                .name(name)
                .description("high definition television")
                .quantity(quantity)
                .price(500.0)
                .build();
        String response = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(product)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return getProductId(response);
    }

    private Integer createProduct2() throws Exception {
        var product = Product.builder()
                .name("pc")
                .description("personal computer")
                .quantity(20)
                .price(1000.0)
                .build();
        String response = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(product)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return getProductId(response);
    }

    private static Integer getProductId(String response) {
        return new Gson().fromJson(response, ProductResponseDTO.class).getId();
    }

    private Integer createOrder(String description) throws Exception {
        var order = Order.builder()
                .description(description)
                .build();
        String response = mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/orders")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(order)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return getOrderId(response);
    }

    private static Integer getOrderId(String response) {
        return new Gson().fromJson(response, OrderResponseDTO.class).getId();
    }

    private void checkingProductLeftOver(Integer productId) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get(format("/api/products/%d", productId))
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("new tv"))
                .andExpect(jsonPath("$.description").value("high definition television"))
                .andExpect(jsonPath("$.quantity").value(9))
                .andExpect(jsonPath("$.price").value(500.0));
    }

    private void checkingProductLeftOverUpdate(Integer productId) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get(format("/api/products/%d", productId))
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("pc"))
                .andExpect(jsonPath("$.description").value("personal computer"))
                .andExpect(jsonPath("$.quantity").value(19))
                .andExpect(jsonPath("$.price").value(1000.0));
    }

    private void checkingProductAbandoned(Integer productId) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get(format("/api/products/%d", productId))
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("new tv"))
                .andExpect(jsonPath("$.description").value("high definition television"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.price").value(500.0));
    }

    private void checkingOrderTotalAmount(Integer orderId) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get(format("/api/orders/%d", orderId))
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.description").value("sales 1"))
                .andExpect(jsonPath("$.totalAmount").value(500.0));
    }

    private void checkingOrderTotalAmountUpdate(Integer orderId) throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get(format("/api/orders/%d", orderId))
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.description").value("sales 1"))
                .andExpect(jsonPath("$.totalAmount").value(1000.0));
    }
}