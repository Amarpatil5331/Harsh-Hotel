package com.hotel.entity.dtos.orders;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillingRequestDto {
    private Boolean applyGst; // true for GST, false for no GST
    private BigDecimal gstPercentage; // GST percentage (e.g., 5.0 for 5%)
    private BigDecimal discountAmount; // discount amount in rupees (optional)
}
