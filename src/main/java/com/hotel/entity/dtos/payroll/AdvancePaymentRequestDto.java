package com.hotel.entity.dtos.payroll;

import lombok.Data;

@Data
public class AdvancePaymentRequestDto {
    private Long userId;
    private Long hotelId;
    private double amount;
    private String description;
}
