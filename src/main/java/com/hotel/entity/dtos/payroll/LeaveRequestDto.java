package com.hotel.entity.dtos.payroll;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class LeaveRequestDto {
    private Long userId;
    private List<LocalDate> leaveDates;
}
