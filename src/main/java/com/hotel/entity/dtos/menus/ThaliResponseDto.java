package com.hotel.entity.dtos.menus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThaliResponseDto {
    private Long thaliId;
    private String thaliName;
    private double thaliPrice;
    private String imageUrl;
    private String imagePublicId;
    private Long hotelId;
    private List<MenuResponseDto> menus;
}
