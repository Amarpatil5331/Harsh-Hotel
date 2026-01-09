package com.hotel.entity.dtos.orders;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingDto {
    private BigDecimal totalAmount;
    private BigDecimal tax;
    private BigDecimal discounts;
    private BigDecimal finalAmount;
}
