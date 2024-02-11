package com.ecommerce.simple.controller;

import com.ecommerce.simple.dto.ProductRequestDTO;
import com.ecommerce.simple.dto.ProductResponseDTO;
import com.ecommerce.simple.model.Product;
import com.ecommerce.simple.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ProductResponseDTO createProduct2(@RequestBody ProductRequestDTO productRequestDTO) {
        log.info("[ createProduct ] productRequestDTO: {}", productRequestDTO);

        ProductResponseDTO productResponseDTO = productService.saveProduct(productRequestDTO);
        log.info("productResponseDTO: {}", productResponseDTO);

        return productResponseDTO;
    }

    @GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public List<ProductResponseDTO> getAllProducts() {
        log.info("[ getAllProducts ]");

        List<ProductResponseDTO> productResponseDTOList = productService.getProducts();
        log.info("productResponseDTOList: {}", productResponseDTOList);

        return productResponseDTOList;
    }

    @GetMapping(value = "/{id}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ProductResponseDTO getProductById(@PathVariable Integer id) {
        log.info("[ getProductById ] id: {}", id);

        ProductResponseDTO productResponseDTO = productService.getProduct(id);
        log.info("productResponseDTO: {}", productResponseDTO);

        return productResponseDTO;
    }

    @PutMapping(value = "/{id}",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ProductResponseDTO updateProductById(@PathVariable Integer id, @RequestBody Product product) {
        log.info("[ updateProductById ] id: {}, product: {}", id, product);

        ProductResponseDTO productResponseDTO = productService.updateProduct(id, product);
        log.info("productResponseDTO: {}", productResponseDTO);

        return productResponseDTO;
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Integer id) {
        log.info("[ deleteProductById ] id: {}", id);

        ResponseEntity<Void> voidResponseEntity = productService.deleteProduct(id);
        log.info("voidResponseEntity: {}", voidResponseEntity);

        return voidResponseEntity;
    }
}
