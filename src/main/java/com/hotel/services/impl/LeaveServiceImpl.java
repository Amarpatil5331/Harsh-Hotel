package com.hotel.services.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hotel.entity.AppUser;
import com.hotel.entity.Leave;
import com.hotel.entity.dtos.payroll.LeaveRequestDto;
import com.hotel.entity.dtos.payroll.LeaveResponseDto;
import com.hotel.repositories.AppUserRepository;
import com.hotel.repositories.LeaveRepository;
import com.hotel.services.LeaveService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRepository leaveRepository;
    private final AppUserRepository appUserRepository;

    @Override
    @Transactional
    public List<LeaveResponseDto> createLeave(LeaveRequestDto requestDto) {
        AppUser user = appUserRepository.findById(requestDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Leave> leaves = new ArrayList<>();
        List<LeaveResponseDto> responses = new ArrayList<>();

        for (LocalDate leaveDate : requestDto.getLeaveDates()) {
            // Check if leave already exists for this date and user
            boolean alreadyExists = leaveRepository.existsByUserIdAndLeaveDate(requestDto.getUserId(), leaveDate);

            if (!alreadyExists) {
                Leave leave = new Leave();
                leave.setUser(user);
                leave.setLeaveDate(leaveDate);
                leave = leaveRepository.save(leave);
                leaves.add(leave);
                responses.add(mapToLeaveResponseDto(leave));
            }
        }

        return responses;
    }

    @Override
    public List<LeaveResponseDto> getLeavesByUser(Long userId) {
        List<Leave> leaves = leaveRepository.findByUserId(userId);
        return leaves.stream()
                .map(this::mapToLeaveResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LeaveResponseDto> getLeavesByUserAndMonth(Long userId, int month, int year) {
        List<Leave> leaves = leaveRepository.findByUserIdAndMonthAndYear(userId, month, year);
        return leaves.stream()
                .map(this::mapToLeaveResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteLeave(Long leaveId) {
        leaveRepository.deleteById(leaveId);
    }

    @Override
    @Transactional
    public void deleteAllLeavesByUser(Long userId) {
        leaveRepository.deleteByUserId(userId);
    }

    @Override
    public int getAbsentDaysCount(Long userId, int month, int year) {
        List<Leave> leaves = leaveRepository.findByUserIdAndMonthAndYear(userId, month, year);
        return leaves.size();
    }

    private LeaveResponseDto mapToLeaveResponseDto(Leave leave) {
        LeaveResponseDto dto = new LeaveResponseDto();
        dto.setId(leave.getId());
        dto.setUserId(leave.getUser().getId());
        dto.setUserName(leave.getUser().getName());
        dto.setLeaveDate(leave.getLeaveDate());
        dto.setCreatedAt(leave.getCreatedAt());
        dto.setUpdatedAt(leave.getUpdatedAt());
        return dto;
    }
}
