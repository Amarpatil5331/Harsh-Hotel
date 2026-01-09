package com.hotel.repositories;

import com.hotel.entity.AdvancePayment;
import com.hotel.entity.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdvancePaymentRepository extends JpaRepository<AdvancePayment, Long> {
    List<AdvancePayment> findByUserIdAndHotelIdOrderByCreatedAtDesc(Long userId, Long hotelId);

    boolean existsByUserIdAndHotelIdAndTypeAndMonthAndYear(Long userId, Long hotelId, TransactionType type, Integer month, Integer year);

    List<AdvancePayment> findByHotelId(Long hotelId);
}
