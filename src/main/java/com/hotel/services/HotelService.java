package com.hotel.services;

import com.hotel.entity.Hotel;
import com.hotel.entity.dtos.hotel.HotelRequestDto;
import com.hotel.entity.dtos.hotel.HotelResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface HotelService {
    public Hotel createHotel(HotelRequestDto requestDto, MultipartFile file);
    public List<HotelResponseDto> getAllHotels();

    public HotelResponseDto getHotelById(Long id);
    public void deleteHotelById(Long id);

    public Hotel updateHotel(HotelResponseDto responseDto, MultipartFile file);
}
