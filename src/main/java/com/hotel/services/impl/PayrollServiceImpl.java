package com.hotel.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.entity.AdvancePayment;
import com.hotel.entity.Payroll;
import com.hotel.entity.dtos.payroll.AdvancePaymentRequestDto;
import com.hotel.entity.dtos.payroll.AdvancePaymentResponseDto;
import com.hotel.entity.dtos.payroll.PayrollRequestDto;
import com.hotel.entity.dtos.payroll.PayrollResponseDto;
import com.hotel.entity.enums.TransactionType;
import com.hotel.repositories.AdvancePaymentRepository;
import com.hotel.repositories.AppUserRepository;
import com.hotel.repositories.HotelRepository;
import com.hotel.repositories.PayrollRepository;
import com.hotel.services.LeaveService;
import com.hotel.services.PayrollService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class PayrollServiceImpl implements PayrollService {

    private final PayrollRepository payrollRepository;
    private final AdvancePaymentRepository advancePaymentRepository;
    private final AppUserRepository appUserRepository;
    private final HotelRepository hotelRepository;
    private final LeaveService leaveService;

    @Override
    @Transactional
    public PayrollResponseDto createOrUpdatePayroll(PayrollRequestDto requestDto) {
        Optional<Payroll> existing = payrollRepository.findByUserIdAndHotelId(requestDto.getUserId(), requestDto.getHotelId());
        Payroll payroll;
        if (existing.isPresent()) {
            payroll = existing.get();
            // For updates, don't change the values, they are managed automatically
        } else {
            payroll = new Payroll();
            payroll.setUser(appUserRepository.findById(requestDto.getUserId()).orElseThrow(() -> new RuntimeException("User not found")));
            payroll.setHotel(hotelRepository.findById(requestDto.getHotelId()).orElseThrow(() -> new RuntimeException("Hotel not found")));
            payroll.setTotalAdvancesTaken(0.0);
            payroll.setTotalAdvancesRemaining(0.0);
            payroll.setLastNetSalary(0.0);
        }
        payroll = payrollRepository.save(payroll);
        return mapToPayrollResponseDto(payroll);
    }

    @Override
    @Transactional
    public AdvancePaymentResponseDto createAdvance(AdvancePaymentRequestDto requestDto) {
        Payroll payroll = payrollRepository.findByUserIdAndHotelId(requestDto.getUserId(), requestDto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Payroll not found"));
        payroll.setTotalAdvancesTaken(payroll.getTotalAdvancesTaken() + requestDto.getAmount());
        payroll.setTotalAdvancesRemaining(payroll.getTotalAdvancesRemaining() + requestDto.getAmount());
        payrollRepository.save(payroll);

        AdvancePayment advance = new AdvancePayment();
        advance.setUser(payroll.getUser());
        advance.setHotel(payroll.getHotel());
        advance.setAmount(requestDto.getAmount());
        advance.setType(TransactionType.ADVANCE);
        advance.setDescription(requestDto.getDescription());
        advance.setRemainingBalanceAfterTransaction(payroll.getTotalAdvancesRemaining());
        advance.setMonth(LocalDate.now().getMonthValue());
        advance.setYear(LocalDate.now().getYear());
        advance = advancePaymentRepository.save(advance);
        return mapToAdvanceResponseDto(advance);
    }

    @Override
    @Transactional
    public PayrollResponseDto paySalary(Long userId, Long hotelId, Integer month, Integer year) {
        if (month == null || year == null) {
            month = LocalDateTime.now().getMonthValue();
            year = LocalDateTime.now().getYear();
        }
        boolean alreadyPaid = advancePaymentRepository.existsByUserIdAndHotelIdAndTypeAndMonthAndYear(userId, hotelId, TransactionType.SALARY, month, year);
        if (alreadyPaid) {
            throw new RuntimeException("Salary already paid for this month");
        }

        Payroll payroll = payrollRepository.findByUserIdAndHotelId(userId, hotelId)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));

        double basicSalary = payroll.getUser().getSalary();

        // Calculate absent days deduction
        int absentDays = getAbsentDaysCount(userId, month, year);
        double perDaySalary = basicSalary / 30.0;
        double absentDeduction = absentDays * perDaySalary;

        // Calculate salary after absent deduction
        double salaryAfterAbsentDeduction = basicSalary - absentDeduction;

        // Calculate advance deduction from the remaining salary after absent deduction
        double advanceDeduction = Math.min(salaryAfterAbsentDeduction, payroll.getTotalAdvancesRemaining());

        // Calculate final net salary (basicSalary - absentDeduction - advanceDeduction)
        double netSalary = salaryAfterAbsentDeduction - advanceDeduction;

        // Update payroll
        payroll.setTotalAdvancesRemaining(payroll.getTotalAdvancesRemaining() - advanceDeduction);
        payroll.setLastNetSalary(netSalary);
        payrollRepository.save(payroll);

        AdvancePayment salaryPayment = new AdvancePayment();
        salaryPayment.setUser(payroll.getUser());
        salaryPayment.setHotel(payroll.getHotel());
        salaryPayment.setAmount(netSalary);
        salaryPayment.setType(TransactionType.SALARY);
        salaryPayment.setDescription(String.format("Salary payment - Absent days: %d, Advance deduction: %.2f", absentDays, advanceDeduction));
        salaryPayment.setRemainingBalanceAfterTransaction(payroll.getTotalAdvancesRemaining());
        salaryPayment.setMonth(month);
        salaryPayment.setYear(year);
        advancePaymentRepository.save(salaryPayment);

        return mapToPayrollResponseDto(payroll);
    }

    @Override
    public PayrollResponseDto getPayroll(Long userId, Long hotelId) {
        Payroll payroll = payrollRepository.findByUserIdAndHotelId(userId, hotelId)
                .orElseThrow(() -> new RuntimeException("Payroll not found"));
        return mapToPayrollResponseDto(payroll);
    }

    @Override
    public List<PayrollResponseDto> getAllPayrolls(Long hotelId) {
        List<Payroll> payrolls = payrollRepository.findByHotelId(hotelId);
        return payrolls.stream().map(this::mapToPayrollResponseDto).collect(Collectors.toList());
    }

    @Override
    public List<AdvancePaymentResponseDto> getAdvanceTransactions(Long userId, Long hotelId) {
        List<AdvancePayment> advances = advancePaymentRepository.findByUserIdAndHotelIdOrderByCreatedAtDesc(userId, hotelId);
        return advances.stream().map(this::mapToAdvanceResponseDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deletePayroll(Long userId, Long hotelId) {
        payrollRepository.deleteByUserIdAndHotelId(userId, hotelId);
        // Also delete related advances
        List<AdvancePayment> advances = advancePaymentRepository.findByUserIdAndHotelIdOrderByCreatedAtDesc(userId, hotelId);
        advancePaymentRepository.deleteAll(advances);
    }

    @Override
    public void deleteAdvance(Long advanceId) {
        advancePaymentRepository.deleteById(advanceId);
    }

    @Override
    public int getAbsentDaysCount(Long userId, int month, int year) {
        return leaveService.getAbsentDaysCount(userId, month, year);
    }

    private PayrollResponseDto mapToPayrollResponseDto(Payroll payroll) {
        PayrollResponseDto dto = new PayrollResponseDto();
        dto.setId(payroll.getId());
        dto.setUserId(payroll.getUser().getId());
        dto.setHotelId(payroll.getHotel().getId());
        dto.setUserName(payroll.getUser().getName());
        dto.setTotalAdvancesTaken(payroll.getTotalAdvancesTaken());
        dto.setTotalAdvancesRemaining(payroll.getTotalAdvancesRemaining());
        dto.setLastNetSalary(payroll.getLastNetSalary());
        dto.setCreatedAt(payroll.getCreatedAt());
        dto.setUpdatedAt(payroll.getUpdatedAt());
        return dto;
    }

    private AdvancePaymentResponseDto mapToAdvanceResponseDto(AdvancePayment advance) {
        AdvancePaymentResponseDto dto = new AdvancePaymentResponseDto();
        dto.setId(advance.getId());
        dto.setUserId(advance.getUser().getId());
        dto.setHotelId(advance.getHotel().getId());
        dto.setAmount(advance.getAmount());
        dto.setType(advance.getType());
        dto.setDescription(advance.getDescription());
        dto.setCreatedAt(advance.getCreatedAt());
        dto.setRemainingBalanceAfterTransaction(advance.getRemainingBalanceAfterTransaction());
        dto.setMonth(advance.getMonth());
        dto.setYear(advance.getYear());
        return dto;
    }
}
