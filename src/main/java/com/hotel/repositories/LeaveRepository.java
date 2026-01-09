package com.hotel.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.hotel.entity.Leave;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {

    List<Leave> findByUserId(Long userId);

    List<Leave> findByUserIdAndLeaveDateBetween(Long userId, LocalDate startDate, LocalDate endDate);

    @Query("SELECT l FROM Leave l WHERE l.user.id = :userId AND YEAR(l.leaveDate) = :year AND MONTH(l.leaveDate) = :month")
    List<Leave> findByUserIdAndMonthAndYear(@Param("userId") Long userId, @Param("month") int month, @Param("year") int year);

    void deleteByUserId(Long userId);

    boolean existsByUserIdAndLeaveDate(Long userId, LocalDate leaveDate);
}
