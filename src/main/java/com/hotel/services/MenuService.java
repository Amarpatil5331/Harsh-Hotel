package com.hotel.services;

import com.hotel.entity.dtos.menus.MenuRequestDto;
import com.hotel.entity.dtos.menus.MenuResponseDto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface MenuService {
    MenuResponseDto createMenu(MenuRequestDto dto,MultipartFile file);
    List<MenuResponseDto> getMenusByHotel(Long hotelId);
    MenuResponseDto updateMenu(Long id, MenuRequestDto dto,MultipartFile file);
    boolean deleteMenu(Long id);
}
