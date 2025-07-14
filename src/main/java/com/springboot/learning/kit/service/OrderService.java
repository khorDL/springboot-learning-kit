package com.springboot.learning.kit.service;

import com.springboot.learning.kit.domain.Order;
import com.springboot.learning.kit.dto.request.OrderRequest;
import com.springboot.learning.kit.event.OrderPlacedEvent;
import com.springboot.learning.kit.exception.DuplicateOrderException;
import com.springboot.learning.kit.producer.OrderEventProducer;
import com.springboot.learning.kit.transformer.OrderEventTransformer;
import com.springboot.learning.kit.transformer.OrderTransformer;
import io.micrometer.core.annotation.Timed;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderTransformer orderTransformer;
    private final OrderEventTransformer orderEventTransformer;
    private final OrderEventProducer orderEventProducer;
    private final EntityManager entityManager;

    @Timed(value = "save.new.order", description = "Time taken to save new order to database")
    public void saveNewOrder(OrderRequest orderRequest, long customerId, long addressId) {
        try {
            log.info("Saving new order: {}", orderRequest);

            Order order = orderTransformer.transformOrderRequestToDomain(orderRequest, customerId, addressId);

            entityManager.persist(order);
            entityManager
                    .flush(); // to ensure changes to entity manager made are written in database and exception if order
            // already exists
        } catch (ConstraintViolationException e) {
            log.error("Error while placing order", e);
            throw new DuplicateOrderException("Order with UUID" + orderRequest.getUUID() + "already exists.");
        }
    }

    public void publishOrderPlacedEvent(OrderRequest orderRequest) {
        log.info("Publishing order placed event for order: {}", orderRequest.getUUID());

        OrderPlacedEvent orderPlacedEvent = orderEventTransformer.transformToOrderPlacedEvent(orderRequest);

        orderEventProducer.sendNewOrderNotificationToVirtualTopic(orderPlacedEvent);
        orderEventProducer.sendNewOrderNotificationToRabbitMQQueue(orderPlacedEvent);
    }
}
