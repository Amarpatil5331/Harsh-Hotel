package com.hotel.controllers;

import com.hotel.entity.dtos.payroll.AdvancePaymentRequestDto;
import com.hotel.entity.dtos.payroll.AdvancePaymentResponseDto;
import com.hotel.entity.dtos.payroll.PayrollRequestDto;
import com.hotel.entity.dtos.payroll.PayrollResponseDto;
import com.hotel.services.PayrollService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payroll")
@AllArgsConstructor
@CrossOrigin("*")
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/create-or-update")
    public ResponseEntity<PayrollResponseDto> createOrUpdatePayroll(@RequestParam Long userId,
                                                                     @RequestParam Long hotelId) {
        PayrollRequestDto requestDto = new PayrollRequestDto();
        requestDto.setUserId(userId);
        requestDto.setHotelId(hotelId);
        PayrollResponseDto response = payrollService.createOrUpdatePayroll(requestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-advance")
    public ResponseEntity<AdvancePaymentResponseDto> createAdvance(@RequestParam Long userId,
                                                                    @RequestParam Long hotelId,
                                                                    @RequestParam double amount,
                                                                    @RequestParam String description) {
        AdvancePaymentRequestDto requestDto = new AdvancePaymentRequestDto();
        requestDto.setUserId(userId);
        requestDto.setHotelId(hotelId);
        requestDto.setAmount(amount);
        requestDto.setDescription(description);
        AdvancePaymentResponseDto response = payrollService.createAdvance(requestDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/pay-salary")
    public ResponseEntity<PayrollResponseDto> paySalary(@RequestParam Long userId,
                                                        @RequestParam Long hotelId,
                                                        @RequestParam(required = false) Integer month,
                                                        @RequestParam(required = false) Integer year) {
        PayrollResponseDto response = payrollService.paySalary(userId, hotelId, month, year);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-payroll")
    public ResponseEntity<PayrollResponseDto> getPayroll(@RequestParam Long userId,
                                                         @RequestParam Long hotelId) {
        PayrollResponseDto response = payrollService.getPayroll(userId, hotelId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-all-payrolls")
    public ResponseEntity<List<PayrollResponseDto>> getAllPayrolls(@RequestParam Long hotelId) {
        List<PayrollResponseDto> response = payrollService.getAllPayrolls(hotelId);
        return !response.isEmpty()? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    @GetMapping("/get-advances")
    public ResponseEntity<List<AdvancePaymentResponseDto>> getAdvanceTransactions(@RequestParam Long userId,
                                                                                   @RequestParam Long hotelId) {
        List<AdvancePaymentResponseDto> response = payrollService.getAdvanceTransactions(userId, hotelId);
        return !response.isEmpty()? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deletePayroll(@RequestParam Long userId,
                                              @RequestParam Long hotelId) {
        payrollService.deletePayroll(userId, hotelId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-advance")
    public ResponseEntity<Void> deleteAdvance(@RequestParam Long advanceId) {
        payrollService.deleteAdvance(advanceId);
        return ResponseEntity.ok().build();
    }
}
