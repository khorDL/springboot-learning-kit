package com.springboot.learning.kit.repository;

import com.springboot.learning.kit.domain.Order;
import com.springboot.learning.kit.domain.OrderItem;
import com.springboot.learning.kit.domain.OrderType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
   /**
    * Find all order items belonging to the given method
    * @param orderId the order's ID
    * @return list of order items
    * */
   List<OrderItem> findByOrderId(Long orderId);

}
