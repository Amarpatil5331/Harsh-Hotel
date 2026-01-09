package com.hotel.repositories;

import com.hotel.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {
    Optional<Payroll> findByUserIdAndHotelId(Long userId, Long hotelId);
    List<Payroll> findByHotelId(Long hotelId);
    void deleteByUserIdAndHotelId(Long userId, Long hotelId);
}
