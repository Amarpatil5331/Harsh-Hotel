package com.hotel.entity.dtos.tables;

import com.hotel.entity.enums.ChairStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChairResponseDto {
    private Long chairId;
    private String chairName;
    private ChairStatus chairStatus;
}
