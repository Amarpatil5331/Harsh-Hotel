package com.hotel.entity.dtos.payroll;

import lombok.Data;

@Data
public class PayrollRequestDto {
    private Long userId;
    private Long hotelId;
}
