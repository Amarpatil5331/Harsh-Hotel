package com.hotel.entity.dtos.menus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ThaliRequestDto {
    private String thaliName;
    private double thaliPrice;
    private Long hotelId;
    private List<Long> menuIds;
}
