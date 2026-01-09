package com.hotel.entity.dtos.tables;

import com.hotel.entity.enums.TableStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TableResponseDto {
    private Long tableId;
    private String tableName;
    private TableStatus tableStatus;
    private Long hotelId;
    private List<ChairResponseDto> chairs;
}