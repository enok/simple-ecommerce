package com.ecommerce.simple.controller;

import com.ecommerce.simple.model.Product;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Testcontainers;

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

    private static String asJsonString(Object object) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            return "";
        }
    }

    @Test
    @Order(1)
    public void getAllProductsNotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/products")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("No products found."));
    }

    @Test
    @Order(2)
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
                        .content(asJsonString(product))
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @Order(3)
    public void getAllProducts() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/products")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @Order(4)
    public void createProductWithoutBody() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .content("")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("Required request body is missing: "));
    }

    @Test
    @Order(5)
    public void createProductWithoutName() throws Exception {
        var product = Product.builder()
                .description("high definition television")
                .quantity(10)
                .price(500.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .content(asJsonString(product))
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[Name is mandatory]"));
    }

    @Test
    @Order(6)
    public void createProductWithoutDescription() throws Exception {
        var product = Product.builder()
                .name("new tv")
                .quantity(10)
                .price(500.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .content(asJsonString(product))
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @Order(7)
    public void createProductWithoutQuantity() throws Exception {
        var product = Product.builder()
                .name("new tv")
                .description("high definition television")
                .price(500.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .content(asJsonString(product))
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[Quantity is mandatory]"));
    }

    @Test
    @Order(8)
    public void createProductWithoutPrice() throws Exception {
        var product = Product.builder()
                .name("new tv")
                .description("high definition television")
                .quantity(10)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .content(asJsonString(product))
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[Price is mandatory]"));
    }

    @Test
    @Order(9)
    public void createProductWithEmptyBody() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .content("{}")
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.httpCode").value(400))
                .andExpect(jsonPath("$.message").value("Bad Request"))
                .andExpect(jsonPath("$.detailedMessage").value("[Name is mandatory, Price is mandatory, Quantity is mandatory]"));
    }

    @Test
    @Order(10)
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

    @Test
    @Order(11)
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

    @Test
    @Order(12)
    public void updateProduct() throws Exception {
        // check previous values
        getProductById();

        var product = Product.builder()
                .name("pc")
                .description("personal computer")
                .quantity(5)
                .price(1000.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/products/1")
                        .contentType("application/json")
                        .content(asJsonString(product))
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("pc"))
                .andExpect(jsonPath("$.description").value("personal computer"))
                .andExpect(jsonPath("$.quantity").value(5))
                .andExpect(jsonPath("$.price").value(1000.0));
    }

    public void createSecondProduct() throws Exception {
        var product = Product.builder()
                .name("keyboard")
                .description("game keyboard")
                .quantity(20)
                .price(100.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/products")
                        .contentType("application/json")
                        .content(asJsonString(product))
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.name").value("keyboard"))
                .andExpect(jsonPath("$.description").value("game keyboard"))
                .andExpect(jsonPath("$.quantity").value(20))
                .andExpect(jsonPath("$.price").value(100.0));
    }

    @Test
    @Order(13)
    public void updateProductTryingToChangeAnotherProduct() throws Exception {
        createSecondProduct();

        var product = Product.builder()
                .id(3L)
                .name("pc")
                .description("personal computer")
                .quantity(5)
                .price(1000.0)
                .build();

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/products/1")
                        .contentType("application/json")
                        .content(asJsonString(product))
                        .accept("application/json"))
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

    @Test
    @Order(14)
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
                        .content(asJsonString(product))
                        .accept("application/json"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Product of id 100 not found."));
    }

    @Test
    @Order(15)
    public void deleteProduct() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/products/3"))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @Order(16)
    public void deleteProductWithIdNotFound() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/products/100"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.httpCode").value(404))
                .andExpect(jsonPath("$.message").value("Not Found"))
                .andExpect(jsonPath("$.detailedMessage").value("Product of id 100 not found."));
    }
}