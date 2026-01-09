package com.hotel.services;

import java.util.List;

import com.hotel.entity.dtos.payroll.AdvancePaymentRequestDto;
import com.hotel.entity.dtos.payroll.AdvancePaymentResponseDto;
import com.hotel.entity.dtos.payroll.PayrollRequestDto;
import com.hotel.entity.dtos.payroll.PayrollResponseDto;

public interface PayrollService {
    PayrollResponseDto createOrUpdatePayroll(PayrollRequestDto requestDto);
    AdvancePaymentResponseDto createAdvance(AdvancePaymentRequestDto requestDto);
    PayrollResponseDto paySalary(Long userId, Long hotelId, Integer month, Integer year);
    PayrollResponseDto getPayroll(Long userId, Long hotelId);
    List<PayrollResponseDto> getAllPayrolls(Long hotelId);
    List<AdvancePaymentResponseDto> getAdvanceTransactions(Long userId, Long hotelId);
    void deletePayroll(Long userId, Long hotelId);
    void deleteAdvance(Long advanceId);
    int getAbsentDaysCount(Long userId, int month, int year);
}
