package com.ecommerce.simple.controller;

import com.ecommerce.simple.dto.OrderItemRequestDTO;
import com.ecommerce.simple.dto.OrderItemResponseDTO;
import com.ecommerce.simple.model.OrderItem;
import com.ecommerce.simple.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order-items")
@RequiredArgsConstructor
@Slf4j
public class OrderItemController {

    private final OrderItemService orderItemService;

    @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public OrderItemResponseDTO createOrderItem(@RequestBody OrderItemRequestDTO orderItemRequestDTO) {
        log.info("[ createOrderItem ] orderItemRequestDTO: {}", orderItemRequestDTO);

        OrderItemResponseDTO orderItemResponseDTO = orderItemService.saveOrderItem(orderItemRequestDTO);
        log.info("orderItemResponseDTO: {}", orderItemResponseDTO);

        return orderItemResponseDTO;
    }

    @GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public List<OrderItemResponseDTO> getAllOrderItems() {
        log.info("[ getAllOrderItems ]");

        List<OrderItemResponseDTO> orderItemResponseDTOList = orderItemService.getOrderItems();
        log.info("orderItemResponseDTOList: {}", orderItemResponseDTOList);

        return orderItemResponseDTOList;
    }

    @GetMapping(value = "/{id}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public OrderItemResponseDTO getOrderItemById(@PathVariable Integer id) {
        log.info("[ getOrderItemById ] id: {}", id);

        OrderItemResponseDTO orderItemResponseDTO = orderItemService.getOrderItem(id);
        log.info("orderItemResponseDTO: {}", orderItemResponseDTO);

        return orderItemResponseDTO;
    }

    @PutMapping(value = "/{id}",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public OrderItemResponseDTO updateOrderItemById(@PathVariable Integer id, @RequestBody OrderItem orderItem) {
        log.info("[ updateOrderItemById ] id: {}, orderItem: {}", id, orderItem);

        OrderItemResponseDTO orderItemResponseDTO = orderItemService.updateOrderItem(id, orderItem);
        log.info("orderItemResponseDTO: {}", orderItemResponseDTO);

        return orderItemResponseDTO;
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteOrderItemById(@PathVariable Integer id) {
        log.info("[ deleteOrderItemById ] id: {}", id);

        ResponseEntity<Void> voidResponseEntity = orderItemService.deleteOrderItem(id);
        log.info("voidResponseEntity: {}", voidResponseEntity);

        return voidResponseEntity;
    }
}
