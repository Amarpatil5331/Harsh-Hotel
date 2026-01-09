package com.hotel.entity.dtos.menus;

import com.hotel.entity.enums.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuRequestDto {
    private String menuName;
    private String description;
    private double price;
    private MenuCategory category;
    // private boolean available;
    private Long hotelId;
}
