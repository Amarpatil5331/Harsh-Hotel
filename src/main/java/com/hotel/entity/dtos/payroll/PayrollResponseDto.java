package com.hotel.entity.dtos.payroll;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PayrollResponseDto {
    private Long id;
    private Long userId;
    private Long hotelId;
    private String userName;
    private double totalAdvancesTaken;
    private double totalAdvancesRemaining;
    private double lastNetSalary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
