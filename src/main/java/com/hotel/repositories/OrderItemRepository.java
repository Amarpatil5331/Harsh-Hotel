package com.hotel.repositories;

import com.hotel.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    java.util.List<OrderItem> findByOrder(com.hotel.entity.Order order);
}
