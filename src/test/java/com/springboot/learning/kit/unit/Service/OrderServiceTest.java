package com.springboot.learning.kit.unit.Service;

import static org.mockito.Mockito.*;

import com.springboot.learning.kit.domain.Order;
import com.springboot.learning.kit.dto.request.OrderRequest;
import com.springboot.learning.kit.service.OrderService;
import com.springboot.learning.kit.transformer.OrderTransformer;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    OrderTransformer orderTransformer;

    @Mock
    EntityManager entityManager;

    @InjectMocks
    OrderService orderService;

    @Test
    void saveNewOrder_success() {
        long customerId = 1L;
        long addressId = 2L;
        OrderRequest request = new OrderRequest();
        Order order = new Order();
        when(orderTransformer.transformOrderRequestToDomain(request, customerId, addressId))
                .thenReturn(order);

        orderService.saveNewOrder(request, customerId, addressId);

        verify(orderTransformer).transformOrderRequestToDomain(request, customerId, addressId);
        verify(entityManager).persist(order);
    }
}
