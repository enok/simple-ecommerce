package com.ecommerce.simple.service;

import com.ecommerce.simple.dto.ProductRequestDTO;
import com.ecommerce.simple.dto.ProductResponseDTO;
import com.ecommerce.simple.exception.DuplicateKeyValueException;
import com.ecommerce.simple.exception.NotFoundException;
import com.ecommerce.simple.model.Product;
import com.ecommerce.simple.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.record.RecordModule;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProductService {

    private final ProductRepository productRepository;

    private static final ModelMapper modelMapper = new ModelMapper();

    static {
        modelMapper.registerModule(new RecordModule());
    }

    public ProductResponseDTO saveProduct(ProductRequestDTO productRequestDTO) {
        log.debug("[ saveProduct ] productRequestDTO: {}", productRequestDTO);

        Product product = modelMapper.map(productRequestDTO, Product.class);
        log.debug("product: {}", product);

        checkIfTheProductExists(product);

        Product productCreated = productRepository.save(product);
        log.debug("productCreated: {}", productCreated);

        ProductResponseDTO productResponseDTO = modelMapper.map(productCreated, ProductResponseDTO.class);
        log.debug("productResponseDTO: {}", productResponseDTO);

        return productResponseDTO;
    }

    public List<ProductResponseDTO> getProducts() {
        log.debug("[ getProducts ]");

        List<Product> allProducts = productRepository.findAll();
        log.debug("allProducts: {}", allProducts);

        if (CollectionUtils.isEmpty(allProducts)) {
            throw new NotFoundException("No products found.");
        }

        List<ProductResponseDTO> orderResponseDTOList = createProductList(allProducts);
        log.debug("orderResponseDTOList: {}", orderResponseDTOList);

        return orderResponseDTOList;
    }

    public ProductResponseDTO getProduct(Integer id) {
        log.debug("[ getProduct ] id: {}", id);

        Optional<Product> productOptional = productRepository.findById(id);
        log.debug("productOptional: {}", productOptional);

        Product product = productOptional.orElseThrow(() -> new NotFoundException(format("Product of id %d not found.", id)));
        log.debug("product: {}", product);

        ProductResponseDTO productResponseDTO = modelMapper.map(product, ProductResponseDTO.class);
        log.debug("productResponseDTO: {}", productResponseDTO);

        return productResponseDTO;
    }

    public ProductResponseDTO updateProduct(Integer id, Product product) {
        log.debug("[ updateProduct ] id: {}, product: {}", id, product);

        // checks if the product exist with such id
        getProduct(id);

        // ensures that product has the correct id
        product.setId(id);

        Product productUpdated = productRepository.save(product);
        log.debug("productUpdated: {}", productUpdated);

        ProductResponseDTO productResponseDTO = modelMapper.map(productUpdated, ProductResponseDTO.class);
        log.debug("productResponseDTO: {}", productResponseDTO);

        return productResponseDTO;
    }

    public ResponseEntity<Void> deleteProduct(Integer id) {
        log.debug("[ deleteProduct ] id: {}", id);

        // checks if the product exist with such id
        getProduct(id);

        productRepository.deleteById(id);
        log.debug("Product deleted: {}", id);

        ResponseEntity<Void> responseEntity = ResponseEntity.noContent().build();
        log.debug("responseEntity: {}", responseEntity);

        return responseEntity;
    }

    private void checkIfTheProductExists(Product product) {
        log.trace("[ checkIfTheProductExists ] product: {}", product);

        productRepository.findByName(product.getName())
                .ifPresent(p -> {
                    throw new DuplicateKeyValueException(format("Product '%s' already exists.", p.getName()));
                });
    }

    private static List<ProductResponseDTO> createProductList(List<Product> products) {
        log.trace("[ createProductList ] products: {}", products);

        return products.stream()
                .map(product -> modelMapper.map(product, ProductResponseDTO.class))
                .collect(Collectors.toList());
    }
}
