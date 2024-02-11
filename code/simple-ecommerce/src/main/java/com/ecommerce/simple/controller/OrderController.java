package com.ecommerce.simple.controller;

import com.ecommerce.simple.dto.OrderRequestDTO;
import com.ecommerce.simple.dto.OrderResponseDTO;
import com.ecommerce.simple.model.Order;
import com.ecommerce.simple.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public OrderResponseDTO createOrder(@RequestBody OrderRequestDTO orderRequestDTO) {
        log.info("[ createOrder ] orderRequestDTO: {}", orderRequestDTO);

        OrderResponseDTO orderResponseDTO = orderService.saveOrder(orderRequestDTO);
        log.info("orderResponseDTO: {}", orderResponseDTO);

        return orderResponseDTO;
    }

    @GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public List<OrderResponseDTO> getAllOrders() {
        log.info("[ getAllOrders ]");

        List<OrderResponseDTO> orderResponseDTOList = orderService.getOrders();
        log.info("orderResponseDTOList: {}", orderResponseDTOList);

        return orderResponseDTOList;
    }

    @GetMapping(value = "/{id}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public OrderResponseDTO getOrderById(@PathVariable Integer id) {
        log.info("[ getOrderById ] id: {}", id);

        OrderResponseDTO orderResponseDTO = orderService.getOrder(id);
        log.info("orderResponseDTO: {}", orderResponseDTO);

        return orderResponseDTO;
    }

    @PutMapping(value = "/{id}",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public OrderResponseDTO updateOrderById(@PathVariable Integer id, @RequestBody Order order) {
        log.info("[ updateOrderById ] id: {}, order: {}", id, order);

        OrderResponseDTO orderResponseDTO = orderService.updateOrder(id, order);
        log.info("orderResponseDTO: {}", orderResponseDTO);

        return orderResponseDTO;
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteOrderById(@PathVariable Integer id) {
        log.info("[ deleteOrderById ] id: {}", id);

        ResponseEntity<Void> voidResponseEntity = orderService.deleteOrder(id);
        log.info("voidResponseEntity: {}", voidResponseEntity);

        return voidResponseEntity;
    }
}
