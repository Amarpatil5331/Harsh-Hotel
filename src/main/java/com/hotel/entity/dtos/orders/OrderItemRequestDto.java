package com.hotel.entity.dtos.orders;

import com.hotel.entity.enums.ItemType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemRequestDto {
    private Long itemId;
    private Integer quantity;
    private ItemType itemType;
}
