package com.hotel.services;

import java.util.List;

import com.hotel.entity.dtos.payroll.LeaveRequestDto;
import com.hotel.entity.dtos.payroll.LeaveResponseDto;

public interface LeaveService {
    List<LeaveResponseDto> createLeave(LeaveRequestDto requestDto);
    List<LeaveResponseDto> getLeavesByUser(Long userId);
    List<LeaveResponseDto> getLeavesByUserAndMonth(Long userId, int month, int year);
    void deleteLeave(Long leaveId);
    void deleteAllLeavesByUser(Long userId);
    int getAbsentDaysCount(Long userId, int month, int year);
}
