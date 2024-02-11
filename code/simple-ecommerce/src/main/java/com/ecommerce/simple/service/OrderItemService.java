package com.ecommerce.simple.service;

import com.ecommerce.simple.dto.OrderItemRequestDTO;
import com.ecommerce.simple.dto.OrderItemResponseDTO;
import com.ecommerce.simple.exception.DuplicateKeyValueException;
import com.ecommerce.simple.exception.MandatoryFieldMissingException;
import com.ecommerce.simple.exception.NoProductLeftOverException;
import com.ecommerce.simple.exception.NotFoundException;
import com.ecommerce.simple.model.Order;
import com.ecommerce.simple.model.OrderItem;
import com.ecommerce.simple.model.Product;
import com.ecommerce.simple.repository.OrderItemRepository;
import com.ecommerce.simple.repository.OrderRepository;
import com.ecommerce.simple.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.record.RecordModule;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    private static final ModelMapper modelMapper = new ModelMapper();

    static {
        modelMapper.registerModule(new RecordModule());
    }

    public OrderItemResponseDTO saveOrderItem(OrderItemRequestDTO orderItemRequestDTO) {
        log.debug("[ saveOrderItem ] orderItemRequestDTO: {}", orderItemRequestDTO);

        return saveOrderItem(orderItemRequestDTO, null);
    }

    public OrderItemResponseDTO saveOrderItem(OrderItemRequestDTO orderItemRequestDTO, Integer orderItemId) {
        log.debug("[ saveOrderItem ] orderItemRequestDTO: {}, orderItemId: {}", orderItemRequestDTO, orderItemId);

        checksMandatoryFields(orderItemRequestDTO);

        OrderItem orderItem = createOrderItem(orderItemRequestDTO, orderItemId);
        log.debug("orderItem: {}", orderItem);

        checkIfTheOrderItemExists(orderItem);

        Product product = getProduct(orderItem.getProductId());
        log.debug("product: {}", product);

        checkProductLeftOver(product);

        OrderItem orderItemCreated = orderItemRepository.save(orderItem);
        log.debug("orderItemCreated: {}", orderItemCreated);

        Order order = updateTotalAmount(orderItem, product);
        log.debug("Order after update total amount: {}", order);

        Product productWithReducedQuantity = reduceProductQuantity(product);
        log.debug("Product after reduce quantity: {}", productWithReducedQuantity);

        OrderItemResponseDTO orderItemResponseDTO = createOrderItemResponse(orderItemCreated, productWithReducedQuantity);
        log.debug("orderItemResponseDTO: {}", orderItemResponseDTO);

        return orderItemResponseDTO;
    }

    public List<OrderItemResponseDTO> getOrderItems() {
        log.debug("[ getOrderItems ]");

        List<OrderItem> allOrderItems = orderItemRepository.findAll();
        log.debug("allOrderItems: {}", allOrderItems);

        if (CollectionUtils.isEmpty(allOrderItems)) {
            throw new NotFoundException("No order items found.");
        }

        List<OrderItemResponseDTO> orderItemResponseDTOList = createOrderItemList(allOrderItems);
        log.debug("orderItemResponseDTOList: {}", orderItemResponseDTOList);

        return orderItemResponseDTOList;
    }

    public OrderItemResponseDTO getOrderItem(Integer id) {
        log.debug("[ getOrderItem ] id: {}", id);

        OrderItem orderItem = orderItemRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException(format("Order item of id %d not found.", id)));
        log.debug("orderItem: {}", orderItem);

        Product product = getProduct(orderItem.getProductId());
        log.debug("product: {}", product);

        OrderItemResponseDTO orderItemResponseDTO = createOrderItemResponse(orderItem, product);
        log.debug("orderItemResponseDTO: {}", orderItemResponseDTO);

        return orderItemResponseDTO;
    }

    public OrderItemResponseDTO updateOrderItem(Integer id, OrderItem orderItem) {
        log.debug("[ updateOrderItem ] id: {}, orderItem: {}", id, orderItem);

        // checks if the item order exist with such id
        OrderItemResponseDTO previousOrderItem = getOrderItem(id);
        log.debug("previousOrderItem: {}", previousOrderItem);

        checksMandatoryFields(orderItem);
        checksExistenceOfForeignKeyRecords(orderItem);

        // checks if product and order were disassociated
        boolean orderRemovedFromOrderItem = !Objects.equals(orderItem.getOrderId(), previousOrderItem.getOrderId());
        boolean productRemovedFromOrderItem = !Objects.equals(orderItem.getProductId(), previousOrderItem.getProductId());

        if (orderRemovedFromOrderItem || productRemovedFromOrderItem) {
            Product previousProduct = getProduct(previousOrderItem.getProductId());
            log.debug("previousProduct: {}", previousProduct);

            Order orderWithTotalAmountUpdated = removeProductValueFromOrderTotalAmount(previousOrderItem, previousProduct);
            log.debug("orderWithTotalAmountUpdated: {}", orderWithTotalAmountUpdated);

            if (productRemovedFromOrderItem) {
                Product productWithIncreasedQuantity = increaseProductQuantity(previousProduct);
                log.debug("productWithIncreasedQuantity: {}", productWithIncreasedQuantity);
            }
        }

        // ensures that item order has the correct id
        orderItem.setId(id);

        OrderItemResponseDTO orderItemResponseDTUpdated = saveOrderItem(modelMapper.map(orderItem, OrderItemRequestDTO.class), id);
        log.debug("orderItemResponseDTUpdated: {}", orderItemResponseDTUpdated);

        return orderItemResponseDTUpdated;
    }

    public ResponseEntity<Void> deleteOrderItem(Integer id) {
        log.debug("[ deleteOrderItem ] id: {}", id);

        // checks if the order item exist with such id
        getOrderItem(id);

        orderItemRepository.deleteById(id);
        log.debug("Order item deleted: {}", id);

        ResponseEntity<Void> responseEntity = ResponseEntity.noContent().build();
        log.debug("responseEntity: {}", responseEntity);

        return responseEntity;
    }

    private void checksExistenceOfForeignKeyRecords(OrderItem orderItem) {
        getProduct(orderItem.getProductId());
        getOrder(orderItem.getOrderId());
    }

    private static void checksMandatoryFields(OrderItemRequestDTO orderItemRequestDTO) {
        log.trace("[ checksMandatoryFields ] orderItemRequestDTO: {}", orderItemRequestDTO);

        StringBuilder message = new StringBuilder();

        if (orderItemRequestDTO.getOrderId() == null) {
            message.append("[")
                    .append("orderId is mandatory");
        }
        if (orderItemRequestDTO.getProductId() == null) {
            if (message.isEmpty()) {
                message.append("[");
            } else {
                message.append(", ");
            }
            message.append("productId is mandatory");
        }
        if (!message.isEmpty()) {
            message.append("]");
            throw new MandatoryFieldMissingException(message.toString());
        }
    }

    private void checksMandatoryFields(OrderItem orderItem) {
        OrderItemRequestDTO orderItemRequestDTO = modelMapper.map(orderItem, OrderItemRequestDTO.class);
        checksMandatoryFields(orderItemRequestDTO);
    }

    private Order removeProductValueFromOrderTotalAmount(OrderItemResponseDTO orderItemResponseDTO, Product productAbandoned) {
        log.trace("[ removeProductValueFromOrderTotalAmount ] orderItemResponseDTO: {}, productAbandoned: {}", orderItemResponseDTO, productAbandoned);

        Order order = getOrder(orderItemResponseDTO.getOrderId());
        order.setTotalAmount(order.getTotalAmount() - productAbandoned.getPrice());
        return orderRepository.save(order);
    }

    private static OrderItem createOrderItem(OrderItemRequestDTO orderItemRequestDTO, Integer orderItemId) {
        log.trace("[ createOrderItem ] orderItemRequestDTO: {}, orderItemId: {}", orderItemRequestDTO, orderItemId);

        return OrderItem.builder()
                .id(orderItemId)
                .orderId(orderItemRequestDTO.getOrderId())
                .productId(orderItemRequestDTO.getProductId())
                .build();
    }

    private List<OrderItemResponseDTO> createOrderItemList(List<OrderItem> allOrderItems) {
        log.trace("[ createOrderItemList ] allOrderItems: {}", allOrderItems);

        Map<Integer, Product> productMap = getProductMap(allOrderItems);

        return allOrderItems.stream()
                .map(orderItem -> {
                    Product product = productMap.get(orderItem.getProductId());

                    return OrderItemResponseDTO.builder()
                            .id(orderItem.getId())
                            .orderId(orderItem.getOrderId())
                            .productId(orderItem.getProductId())
                            .productName(product.getName())
                            .productDescription(product.getDescription())
                            .productPrice(product.getPrice())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private Map<Integer, Product> getProductMap(List<OrderItem> allOrderItems) {
        log.trace("[ getProductMap ] allOrderItems: {}", allOrderItems);

        List<Integer> productIds = allOrderItems.stream()
                .map(OrderItem::getProductId)
                .toList();
        List<Product> productList = productRepository.findAllById(productIds);
        return productList.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private void checkIfTheOrderItemExists(OrderItem orderItem) {
        log.trace("[ checkIfTheOrderItemExists ] orderItem: {}", orderItem);

        orderItemRepository.findByProductIdAndOrderId(orderItem.getProductId(), orderItem.getOrderId())
                .ifPresent(p -> {
                    throw new DuplicateKeyValueException(format("Order item already exists for order: '%d' and product: '%d'.", p.getOrderId(), p.getProductId()));
                });
    }

    private static void checkProductLeftOver(Product product) {
        log.trace("[ checkProductLeftOver ] product: {}", product);

        Integer quantity = product.getQuantity();
        if (quantity <= 0) {
            throw new NoProductLeftOverException(format("There is no left over products, quantity: %d", quantity));
        }
    }

    private Order updateTotalAmount(OrderItem orderItem, Product productFound) {
        log.trace("[ updateTotalAmount ] orderItem: {}, productFound: {}", orderItem, productFound);

        Order order = getOrder(orderItem.getOrderId());
        Double totalAmount = order.getTotalAmount() + productFound.getPrice();
        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    private Product reduceProductQuantity(Product product) {
        log.trace("[ reduceProductQuantity ] product: {}", product);

        product.setQuantity(product.getQuantity() - 1);
        return productRepository.save(product);
    }

    private static OrderItemResponseDTO createOrderItemResponse(OrderItem orderItem, Product product) {
        log.trace("[ createOrderItemResponse ] orderItem: {}, product: {}", orderItem, product);

        return OrderItemResponseDTO.builder()
                .id(orderItem.getId())
                .orderId(orderItem.getOrderId())
                .productId(orderItem.getProductId())
                .productName(product.getName())
                .productDescription(product.getDescription())
                .productPrice(product.getPrice())
                .build();
    }

    private Product increaseProductQuantity(Product product) {
        log.trace("[ increaseProductQuantity ] product: {}", product);

        product.setQuantity(product.getQuantity() + 1);
        return productRepository.save(product);
    }

    private Order getOrder(Integer orderId) {
        log.trace("[ getOrder ] orderId: {}", orderId);

        return orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException(format("Order of id %d not found.", orderId)));
    }

    private Product getProduct(Integer productId) {
        log.trace("[ getProduct ] productId: {}", productId);

        return productRepository.findById(productId).orElseThrow(() -> new NotFoundException(format("Product of id %d not found.", productId)));
    }
}
