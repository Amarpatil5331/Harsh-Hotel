package com.hotel.entity.dtos.tables;

import com.hotel.entity.enums.ChairStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChairRequestDto {
    private ChairStatus chairStatus; // usually AVAILABLE when created
}
