package com.hotel.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hotel.entity.Order;
import com.hotel.entity.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o WHERE o.hotelId = :hotelId AND (:status IS NULL OR o.status = :status)")
    List<Order> findByHotelIdAndStatus(@Param("hotelId") Long hotelId, @Param("status") OrderStatus status);
}
