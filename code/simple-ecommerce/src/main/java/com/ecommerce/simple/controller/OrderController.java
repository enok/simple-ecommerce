package com.ecommerce.simple.controller;

import com.ecommerce.simple.exception.NotFoundException;
import com.ecommerce.simple.model.Order;
import com.ecommerce.simple.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderRepository orderRepository;

    @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Order createOrder(@RequestBody Order order) {
        log.debug("[ createOrder ] order: {}", order);

        Order orderCreated = orderRepository.save(order);
        log.debug("Order created: {}", orderCreated);

        return orderCreated;
    }

    @GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public List<Order> getAllOrders() {
        log.debug("[ getAllOrders ]");
        List<Order> allOrders = orderRepository.findAll();
        log.debug("Orders found: {}", allOrders);

        if (CollectionUtils.isEmpty(allOrders)) {
            throw new NotFoundException("No orders found.");
        }

        return allOrders;
    }

    @GetMapping(value = "/{id}",
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Order getOrderById(@PathVariable Integer id) {
        log.debug("[ getOrderById ] id: {}", id);

        Optional<Order> order = orderRepository.findById(id);
        log.debug("Order found: {}", order);

        return order.orElseThrow(() -> new NotFoundException(format("Order of id %d not found.", id)));
    }

    @PutMapping(value = "/{id}",
            consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE},
            produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Order updateOrderById(@PathVariable Integer id, @RequestBody Order order) {
        log.debug("[ updateOrderById ] id: {}, order: {}", id, order);

        // checks if the order exist with such id
        getOrderById(id);

        // ensures that order has the correct id
        order.setId(id);

        Order orderUpdated = orderRepository.save(order);
        log.debug("Order updated: {}", orderUpdated);

        return orderUpdated;
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteOrderById(@PathVariable Integer id) {
        log.debug("[ deleteOrderById ] id: {}", id);

        // checks if the order exist with such id
        getOrderById(id);

        orderRepository.deleteById(id);
        log.debug("Order deleted: {}", id);

        return ResponseEntity.noContent().build();
    }
}
