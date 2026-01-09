package com.hotel.entity.dtos.orders;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
    private Long hotelId;
    private List<Long> tableIds;
    private List<Long> chairIds;
    private List<OrderItemRequestDto> items;
    private String customerContact; // Customer contact number for the order
}
