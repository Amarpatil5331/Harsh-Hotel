package com.hotel.entity.dtos.orders;

import java.time.LocalDateTime;
import java.util.List;

import com.hotel.entity.enums.OrderStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private Long orderId;
    private String orderReference;
    private String hotelName;
    private Long hotelId;
    private String hotelAddress;
    private List<OrderTableDto> orderTables;
    private OrderStatus status;
    private BillingDto billingSnapshot;
    private String customerContact; // Customer contact number for the order
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<OrderItemResponseDto> items;
}
