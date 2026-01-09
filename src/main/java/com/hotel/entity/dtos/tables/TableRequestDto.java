package com.hotel.entity.dtos.tables;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableRequestDto {
    private String tableName;
    private Long hotelId;  // manager selects which hotel branch
    private List<ChairRequestDto> chairs=new ArrayList<>();;
}
