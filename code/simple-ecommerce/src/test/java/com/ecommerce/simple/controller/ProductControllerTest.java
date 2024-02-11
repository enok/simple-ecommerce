package com.ecommerce.simple.controller;

import com.ecommerce.simple.exception.CustomExceptionHandler;
import com.ecommerce.simple.model.Product;
import com.ecommerce.simple.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
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
class ProductControllerTest {

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
    @Order(1)
    public void createProduct() throws Exception {
        var product = Product.builder()
                .name("new tv")
                .description("high definition television")
                .quantity(10)
                .price(500.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(product)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    /**
     * 200
     */
    @Test
    @Order(2)
    public void getAllProducts() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/products")
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
    @Order(3)
    public void createProductWithoutDescription() throws Exception {
        var product = Product.builder()
                .name("new tv 1")
                .quantity(10)
                .price(500.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(product)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    /**
     * 200
     */
    @Test
    @Order(4)
    public void getProductById() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/products/1")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("new tv"))
                .andExpect(jsonPath("$.description").value("high definition television"))
                .andExpect(jsonPath("$.quantity").value(10))
                .andExpect(jsonPath("$.price").value(500.));
    }

    /**
     * 200
     */
    @Test
    @Order(5)
    public void updateProduct() throws Exception {
        var product = Product.builder()
                .name("pc")
                .description("personal computer")
                .quantity(5)
                .price(1000.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/products/1")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(product)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("pc"))
                .andExpect(jsonPath("$.description").value("personal computer"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.price").value(1000.0));
    }

    /**
     * 200
     */
    @Test
    @Order(6)
    public void updateProductTryingToChangeAnotherProduct() throws Exception {
        var product1 = Product.builder()
                .name("keyboard")
                .description("game keyboard")
                .quantity(20)
                .price(100.0)
                .build();
        mockMvc.perform(MockMvcRequestBuilders
                .post("/api/products")
                .contentType("application/json")
                .accept("application/json")
                .content(asJsonString(product1)));

        var product = Product.builder()
                .id(2)
                .name("pc")
                .description("personal computer")
                .quantity(5)
                .price(1000.0)
                .build();
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/products/1")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(product)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("pc"))
                .andExpect(jsonPath("$.description").value("personal computer"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.price").value(1000.0));

        // second not altered
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/products/3")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("keyboard"))
                .andExpect(jsonPath("$.description").value("game keyboard"))
                .andExpect(jsonPath("$.quantity").value(20))
                .andExpect(jsonPath("$.price").value(100.0));
    }

    /**
     * 200
     */
    @Test
    @Order(7)
    public void deleteProduct() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/products/2"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    /**
     * 400
     */
    @Test
    @Order(8)
    public void createProductWithSameName() throws Exception {
        var product = Product.builder()
                .name("pc")
                .description("high definition television")
                .quantity(10)
                .price(500.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(product)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("Product 'pc' already exists."));
    }

    /**
     * 400
     */
    @Test
    @Order(9)
    public void createProductWithoutBody() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
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
    @Order(10)
    public void createProductWithoutName() throws Exception {
        var product = Product.builder()
                .description("high definition television")
                .quantity(10)
                .price(500.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(product)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[name is mandatory]"));
    }

    /**
     * 400
     */
    @Test
    @Order(11)
    public void createProductWithoutQuantity() throws Exception {
        var product = Product.builder()
                .name("new tv 3")
                .description("high definition television")
                .price(500.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(product)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[quantity is mandatory]"));
    }

    /**
     * 400
     */
    @Test
    @Order(12)
    public void createProductWithoutPrice() throws Exception {
        var product = Product.builder()
                .name("new tv 4")
                .description("high definition television")
                .quantity(10)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(product)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[price is mandatory]"));
    }

    /**
     * 400
     */
    @Test
    @Order(13)
    public void createProductWithEmptyBody() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .accept("application/json")
                        .content("{}"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[name is mandatory, price is mandatory, quantity is mandatory]"));
    }

    /**
     * 404
     */
    @Test
    @Order(14)
    public void deleteProductNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/products/2"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Product of id 2 not found."));
    }

    /**
     * 404
     */
    @Test
    @Order(15)
    public void getAllProductsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/products/1"));
        mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/products/3"));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/products")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("No products found."));
    }

    /**
     * 404
     */
    @Test
    @Order(16)
    public void getProductByIdNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/products/100")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Product of id 100 not found."));
    }

    /**
     * 404
     */
    @Test
    @Order(17)
    public void updateProductWithIdNotFound() throws Exception {
        var product = Product.builder()
                .name("pc")
                .description("personal computer")
                .quantity(5)
                .price(1000.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/products/100")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(product)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Product of id 100 not found."));
    }

    /**
     * 404
     */
    @Test
    @Order(18)
    public void deleteProductWithIdNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/products/100"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Product of id 100 not found."));
    }

    /**
     * 404
     */
    @Test
    @Order(19)
    public void deleteProductWithNotExistingPath() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/products2"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("No static resource api/products2."));
    }

    /**
     * 405
     */
    @Test
    @Order(20)
    public void methodNoAllowed() throws Exception {
        var product = Product.builder()
                .name("new tv")
                .description("high definition television")
                .quantity(10)
                .price(500.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .patch("/api/products")
                        .contentType("application/json")
                        .accept("application/json")
                        .content(asJsonString(product)))
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
    @Order(21)
    public void acceptNotSet() throws Exception {
        var product = Product.builder()
                .name("new tv")
                .description("high definition television")
                .quantity(10)
                .price(500.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .content(asJsonString(product)))
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
    @Order(22)
    public void contentTypedNotSupported() throws Exception {
        var product = Product.builder()
                .name("new tv")
                .description("high definition television")
                .quantity(10)
                .price(500.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json2")
                        .accept("application/json")
                        .content(asJsonString(product)))
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
    @Order(23)
    public void notMappedException() throws Exception {
        ProductService productService = Mockito.mock(ProductService.class);
        ProductController productController = new ProductController(productService);

        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();

        given(productService.getProducts())
                .willAnswer(invocation -> {
                    throw new RuntimeException("Generic error.");
                });

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/products")
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
    @Order(24)
    public void serviceUnavailable() throws Exception {
        ProductService productService = Mockito.mock(ProductService.class);
        ProductController productController = new ProductController(productService);

        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new CustomExceptionHandler())
                .build();

        given(productService.getProducts())
                .willAnswer(invocation -> {
                    throw new ConnectException("Connection error.");
                });

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/products")
                        .contentType("application/json")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.httpCode").value(503))
                .andExpect(jsonPath("$.message").value("Service Unavailable"))
                .andExpect(jsonPath("$.detailedMessage").value("Connection error."));
    }
}