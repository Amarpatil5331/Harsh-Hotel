package com.hotel.entity.dtos.tables;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableUpdateDto {
    private String tableName;   // optional rename
    private Integer chairCount; // new total chairs count
}
