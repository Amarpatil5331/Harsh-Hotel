package com.hotel.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.entity.dtos.payroll.LeaveRequestDto;
import com.hotel.entity.dtos.payroll.LeaveResponseDto;
import com.hotel.services.LeaveService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/leave")
@AllArgsConstructor
@CrossOrigin("*")
public class LeaveController {

    private final LeaveService leaveService;

    @PostMapping("/create-leave")
    public ResponseEntity<?> createLeave(@RequestBody LeaveRequestDto requestDto) {
        List<LeaveResponseDto> response = leaveService.createLeave(requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-leaves-by-user-id")
    public ResponseEntity<?> getLeavesByUser(@RequestParam Long userId) {
        List<LeaveResponseDto> response = leaveService.getLeavesByUser(userId);
        return !response.isEmpty() ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    @GetMapping("/get-leaves-by-user-and-month-and-year")
    public ResponseEntity<?> getLeavesByUserAndMonth(
            @RequestParam Long userId,
            @RequestParam int month,
            @RequestParam int year) {
        List<LeaveResponseDto> response = leaveService.getLeavesByUserAndMonth(userId, month, year);
        return !response.isEmpty() ? ResponseEntity.ok(response) : ResponseEntity.noContent().build();
    }

    @GetMapping("/get-absent-days-count")
    public ResponseEntity<?> getAbsentDaysCount(
            @RequestParam Long userId,
            @RequestParam int month,
            @RequestParam int year) {
        int absentDays = leaveService.getAbsentDaysCount(userId, month, year);
        return ResponseEntity.ok(absentDays);
    }

    @DeleteMapping("/delete-leave")
    public ResponseEntity<?> deleteLeave(@RequestParam Long leaveId) {
        leaveService.deleteLeave(leaveId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-all-leaves-by-user")
    public ResponseEntity<?> deleteAllLeavesByUser(@RequestParam Long userId) {
        leaveService.deleteAllLeavesByUser(userId);
        return ResponseEntity.ok().build();
    }
}
