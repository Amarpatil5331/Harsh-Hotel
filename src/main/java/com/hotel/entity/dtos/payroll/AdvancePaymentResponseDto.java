package com.hotel.entity.dtos.payroll;

import com.hotel.entity.enums.TransactionType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdvancePaymentResponseDto {
    private Long id;
    private Long userId;
    private Long hotelId;
    private double amount;
    private TransactionType type;
    private String description;
    private LocalDateTime createdAt;
    private double remainingBalanceAfterTransaction;

    private Integer month;
    private Integer year;
}
