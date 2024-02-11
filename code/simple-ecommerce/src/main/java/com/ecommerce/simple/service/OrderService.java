package com.ecommerce.simple.service;

import com.ecommerce.simple.dto.OrderRequestDTO;
import com.ecommerce.simple.dto.OrderResponseDTO;
import com.ecommerce.simple.exception.NotFoundException;
import com.ecommerce.simple.model.Order;
import com.ecommerce.simple.repository.OrderRepository;
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
public class OrderService {

    private final OrderRepository orderRepository;

    private static final ModelMapper modelMapper = new ModelMapper();

    static {
        modelMapper.registerModule(new RecordModule());
    }

    public OrderResponseDTO saveOrder(OrderRequestDTO orderRequestDTO) {
        log.debug("[ saveOrder ] orderRequestDTO: {}", orderRequestDTO);

        Order order = createOrder(orderRequestDTO);
        log.debug("order: {}", order);

        Order orderSaved = orderRepository.save(order);
        log.debug("orderSaved: {}", orderSaved);

        OrderResponseDTO orderResponseDTO = modelMapper.map(orderSaved, OrderResponseDTO.class);
        log.debug("orderResponseDTO: {}", orderResponseDTO);

        return orderResponseDTO;
    }

    public List<OrderResponseDTO> getOrders() {
        log.debug("[ getOrders ]");

        List<Order> allOrders = orderRepository.findAll();
        log.debug("allOrders: {}", allOrders);

        if (CollectionUtils.isEmpty(allOrders)) {
            throw new NotFoundException("No orders found.");
        }

        List<OrderResponseDTO> orderResponseDTOList = createOrderList(allOrders);
        log.debug("orderResponseDTOList: {}", orderResponseDTOList);

        return orderResponseDTOList;
    }

    public OrderResponseDTO getOrder(Integer id) {
        log.debug("[ getOrder ] id: {}", id);

        Optional<Order> orderOptional = orderRepository.findById(id);
        Order order = orderOptional.orElseThrow(() -> new NotFoundException(format("Order of id %d not found.", id)));
        log.debug("order: {}", order);

        OrderResponseDTO orderResponseDTO = modelMapper.map(order, OrderResponseDTO.class);
        log.debug("orderResponseDTO: {}", orderResponseDTO);

        return orderResponseDTO;
    }

    public OrderResponseDTO updateOrder(Integer id, Order order) {
        log.debug("[ updateOrder ] id: {}, order: {}", id, order);

        // checks if the order exist with such id
        getOrder(id);

        // ensures that order has the correct id
        order.setId(id);

        Order orderUpdated = orderRepository.save(order);
        log.debug("orderUpdated: {}", orderUpdated);

        OrderResponseDTO orderResponseDTO = modelMapper.map(orderUpdated, OrderResponseDTO.class);
        log.debug("orderResponseDTO: {}", orderResponseDTO);

        return orderResponseDTO;
    }

    public ResponseEntity<Void> deleteOrder(Integer id) {
        log.debug("[ deleteOrder ] id: {}", id);

        // checks if the order exist with such id
        getOrder(id);

        orderRepository.deleteById(id);
        log.debug("Order deleted: {}", id);

        ResponseEntity<Void> responseEntity = ResponseEntity.noContent().build();
        log.debug("responseEntity: {}", responseEntity);

        return responseEntity;
    }

    private static List<OrderResponseDTO> createOrderList(List<Order> orders) {
        log.trace("[ createOrderList ] orders: {}", orders);

        return orders.stream()
                .map(order -> modelMapper.map(order, OrderResponseDTO.class))
                .collect(Collectors.toList());
    }

    private static Order createOrder(OrderRequestDTO orderRequestDTO) {
        log.trace("[ createOrder ] orderRequestDTO: {}", orderRequestDTO);

        return Order.builder()
                .description(orderRequestDTO.description())
                .totalAmount(0.0)
                .build();
    }

}
