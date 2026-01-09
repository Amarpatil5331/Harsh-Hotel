package com.hotel.entity.dtos.orders;

import com.hotel.entity.enums.ItemType;
import com.hotel.entity.enums.OrderItemStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponseDto {
    private Long orderItemId;
    private String itemName;
    private ItemType itemType;
    private BigDecimal priceAtOrder;
    private Integer quantity;
    private OrderItemStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
