package com.hotel.services.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hotel.entity.Hotel;
import com.hotel.entity.Menu;
import com.hotel.entity.dtos.menus.MenuRequestDto;
import com.hotel.entity.dtos.menus.MenuResponseDto;
import com.hotel.repositories.HotelRepository;
import com.hotel.repositories.MenuRepository;
import com.hotel.repositories.ThaliRepository;
import com.hotel.services.MenuService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final HotelRepository hotelRepository;
    private final ThaliRepository thaliRepository;
    private final ImageUploadService imageUploadService;

    @Override
    public MenuResponseDto createMenu(MenuRequestDto dto,MultipartFile file) {
        Hotel hotel = hotelRepository.findById(dto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

         Map<String, String> imageMap;
        try {
            if (file != null && !file.isEmpty()) {
                imageMap = imageUploadService.uploadMenuImage(file);
            } else {
                ClassPathResource imgFile = new ClassPathResource("static/menu.png");
                imageMap = imageUploadService.uploadMenuImage(imgFile.getFile());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Menu menu = new Menu();
        menu.setMenuName(dto.getMenuName());
        menu.setDescription(dto.getDescription());
        menu.setPrice(dto.getPrice());
        menu.setMenuCategory(dto.getCategory());
        // menu.setAvailable(true);
        menu.setImageUrl(imageMap.get("url"));
        menu.setImagePublicId(imageMap.get("publicId"));
        menu.setHotel(hotel);

        return mapToResponse(menuRepository.save(menu));
    }

    @Override
    public List<MenuResponseDto> getMenusByHotel(Long hotelId) {
        return menuRepository.findByHotel_Id(hotelId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // @Override
    // @Transactional
    // public MenuResponseDto updateMenu(Long id, MenuRequestDto dto) {
    //     Menu menu = menuRepository.findById(id)
    //             .orElseThrow(() -> new RuntimeException("Menu not found"));

        
    //     menu.setMenuName(dto.getMenuName());
    //     menu.setDescription(dto.getDescription());
    //     menu.setPrice(dto.getPrice());
    //     menu.setMenuCategory(dto.getCategory());
    //     menu.setAvailable(dto.isAvailable());

    //     return mapToResponse(menuRepository.save(menu));
    // }


    @Override
    @Transactional
    public MenuResponseDto updateMenu(Long id, MenuRequestDto dto, MultipartFile file) {
        Menu existingMenu = menuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Menu not found"));

        String imgUrl = existingMenu.getImageUrl();
        String publicId = existingMenu.getImagePublicId();

        Map<String, String> imageMap;
        try {
            if (file != null && !file.isEmpty()) {
                // delete old image if exists
                if (publicId != null && imgUrl != null) {
                    imageUploadService.deleteImage(publicId);
                }
                // upload new image
                imageMap = imageUploadService.uploadMenuImage(file);
                existingMenu.setImageUrl(imageMap.get("url"));
                existingMenu.setImagePublicId(imageMap.get("publicId"));
            } else {
                // keep old image if no new file is uploaded
                existingMenu.setImageUrl(imgUrl);
                existingMenu.setImagePublicId(publicId);
            }
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }

        existingMenu.setMenuName(dto.getMenuName());
        existingMenu.setDescription(dto.getDescription());
        existingMenu.setPrice(dto.getPrice());
        existingMenu.setMenuCategory(dto.getCategory());
        // existingMenu.setAvailable(dto.isAvailable());

        return mapToResponse(menuRepository.save(existingMenu));
    }


    @Override
    @Transactional
    public boolean deleteMenu(Long id) {
        if (!menuRepository.existsById(id))
            return false;

        if (thaliRepository.countThalisByMenuId(id) > 0) {
            return false;
        }

        Menu menu = menuRepository.findById(id).get();
        try{
            imageUploadService.deleteImage(menu.getImagePublicId());
        }
        catch(Exception e){
            throw new RuntimeException("Failed to delete image from Cloudinary", e);
        }
        menuRepository.deleteById(id);
        return true;
    }

    private MenuResponseDto mapToResponse(Menu menu) {
        return MenuResponseDto.builder()
                .menuId(menu.getMenuId())
                .menuName(menu.getMenuName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .category(menu.getMenuCategory())
                // .available(menu.isAvailable())
                .imageUrl(menu.getImageUrl())
                .imagePublicId(menu.getImagePublicId())
                .hotelId(menu.getHotel().getId())
                .build();
    }
}

