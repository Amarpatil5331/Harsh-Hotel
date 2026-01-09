package com.hotel.services;

import com.hotel.entity.dtos.menus.ThaliRequestDto;
import com.hotel.entity.dtos.menus.ThaliResponseDto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ThaliService {
    ThaliResponseDto createThali(ThaliRequestDto dto,MultipartFile file);
    List<ThaliResponseDto> getThalisByHotel(Long hotelId);
    ThaliResponseDto updateThali(Long id, ThaliRequestDto dto,MultipartFile file);
    boolean deleteThali(Long id);
}

