package com.springboot.learning.kit.unit.Service;

import au.com.origin.snapshots.Expect;
import au.com.origin.snapshots.junit5.SnapshotExtension;
import com.springboot.learning.kit.domain.Order;
import com.springboot.learning.kit.domain.OrderItem;
import com.springboot.learning.kit.domain.OrderType;
import com.springboot.learning.kit.dto.response.OrderStatusResponse;
import com.springboot.learning.kit.repository.OrderItemRepository;
import com.springboot.learning.kit.repository.OrderRepository;
import com.springboot.learning.kit.service.OrderStatusService;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, SnapshotExtension.class})
public class OrderStatusServiceTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private OrderStatusService orderStatusService;

    private Expect expect;

    @Test
    void verifyOrderStatusResponse(){
        //arrange
        long orderUUID = 1L;

        // Mock the repository method that fetches the order
        when(orderRepository.findById(orderUUID)).thenReturn(Optional.of(createOrder(orderUUID)));

        // Mock the repository method that fetches the order items
        when(orderItemRepository.findByOrderId(orderUUID)).thenReturn(createOrderItems(orderUUID));

        //act
        OrderStatusResponse status = orderStatusService.getOrderStatus(orderUUID);

        //assert
        expect.serializer("json").toMatchSnapshot(status);
    }

    private @NotNull Order createOrder(long orderUUID) {
        return Order.builder()
                .uuid(orderUUID)
                .orderType(OrderType.ONLINE)
                .customerDetailsId(101L)
                .customerAddressId(201L)
                .totalAmount(BigDecimal.valueOf(99.99))
                .currency("GBP")
                .orderCreated(java.time.LocalDateTime.now())
                .build();
    }

    private @NotNull List<OrderItem> createOrderItems(long orderId) {
        return List.of(
                OrderItem.builder()
                        .orderId(orderId)
                        .productId(101L)
                        .quantity(2)
                        .status("CONFIRMED")
                        .pricePerUnit(BigDecimal.valueOf(49.99))
                        .build(),
                OrderItem.builder()
                        .orderId(orderId)
                        .productId(102L)
                        .quantity(1)
                        .status("PENDING")
                        .pricePerUnit(BigDecimal.valueOf(10.50))
                        .build()
        );
    }
}
