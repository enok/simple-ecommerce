package com.ecommerce.simple.controller;

import com.ecommerce.simple.exception.DuplicateKeyValueException;
import com.ecommerce.simple.exception.NotFoundException;
import com.ecommerce.simple.model.Product;
import com.ecommerce.simple.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static java.lang.String.*;
import static java.lang.String.format;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductRepository productRepository;

    @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Product createProduct(@RequestBody Product product) {
        log.debug("[ createProduct ] product: {}", product);

        checkIfTheProductAlreadyExists(product);

        Product productCreated = productRepository.save(product);
        log.debug("Product created: {}", product);

        return productCreated;
    }

    private void checkIfTheProductAlreadyExists(Product product) {
        productRepository.findByName(product.getName())
                .ifPresent(p -> {
                    throw new DuplicateKeyValueException(format("Product '%s' already exists.", p.getName()));
                });
    }

    @GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public List<Product> getAllProducts() {
        log.debug("[ getAllProducts ]");
        List<Product> allProducts = productRepository.findAll();
        log.debug("Products found: {}", allProducts);

        if (CollectionUtils.isEmpty(allProducts)) {
            throw new NotFoundException("No products found.");
        }

        return allProducts;
    }

    @GetMapping(value = "/{id}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Product getProductById(@PathVariable Integer id) {
        log.debug("[ getProductById ] id: {}", id);

        Optional<Product> product = productRepository.findById(id);
        log.debug("Product found: {}", product);

        return product.orElseThrow(() -> new NotFoundException(format("Product of id %d not found.", id)));
    }

    @PutMapping(value = "/{id}",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Product updateProductById(@PathVariable Integer id, @RequestBody Product product) {
        log.debug("[ updateProductById ] id: {}, product: {}", id, product);

        // checks if the product exist with such id
        getProductById(id);

        // ensures that product has the correct id
        product.setId(id);

        Product productUpdated = productRepository.save(product);
        log.debug("Product updated: {}", productUpdated);

        return productUpdated;
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteProductById(@PathVariable Integer id) {
        log.debug("[ deleteProductById ] id: {}", id);

        // checks if the product exist with such id
        getProductById(id);

        productRepository.deleteById(id);
        log.debug("Product deleted: {}", id);

        return ResponseEntity.noContent().build();
    }
}
