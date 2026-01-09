package com.hotel.entity.dtos.payroll;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class LeaveResponseDto {
    private Long id;
    private Long userId;
    private String userName;
    private LocalDate leaveDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
