package com.hotel.entity.dtos.menus;

import com.hotel.entity.enums.MenuCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponseDto {
    private Long menuId;
    private String menuName;
    private String description;
    private double price;
    private MenuCategory category;
    private String imageUrl;
    private String imagePublicId;
    // private boolean available;
    private Long hotelId;
}
