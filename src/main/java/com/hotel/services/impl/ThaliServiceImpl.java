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
import com.hotel.entity.Thali;
import com.hotel.entity.dtos.menus.MenuResponseDto;
import com.hotel.entity.dtos.menus.ThaliRequestDto;
import com.hotel.entity.dtos.menus.ThaliResponseDto;
import com.hotel.repositories.HotelRepository;
import com.hotel.repositories.MenuRepository;
import com.hotel.repositories.ThaliRepository;
import com.hotel.services.ThaliService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ThaliServiceImpl implements ThaliService {

    private final ThaliRepository thaliRepository;
    private final MenuRepository menuRepository;
    private final HotelRepository hotelRepository;
    private final ImageUploadService imageUploadService;

    @Override
    public ThaliResponseDto createThali(ThaliRequestDto dto,MultipartFile file) {
        Hotel hotel = hotelRepository.findById(dto.getHotelId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        List<Menu> menus = menuRepository.findAllById(dto.getMenuIds());

        Map<String, String> imageMap;
        try {
            if (file != null && !file.isEmpty()) {
                imageMap = imageUploadService.uploadThaliImage(file);
            } else {
                ClassPathResource imgFile = new ClassPathResource("static/thali.jpeg");
                imageMap = imageUploadService.uploadThaliImage(imgFile.getFile());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Thali thali = new Thali();
        thali.setThaliName(dto.getThaliName());
        thali.setThaliPrice(dto.getThaliPrice());
        thali.setImageUrl(imageMap.get("url"));
        thali.setImagePublicId(imageMap.get("publicId"));
        thali.setHotel(hotel);
        thali.setMenus(menus);

        return mapToResponse(thaliRepository.save(thali));
    }

    @Override
    public List<ThaliResponseDto> getThalisByHotel(Long hotelId) {
        return thaliRepository.findByHotel_Id(hotelId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ThaliResponseDto updateThali(Long id, ThaliRequestDto dto,MultipartFile file) {
        Thali existingThali = thaliRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thali not found"));

        List<Menu> menus = menuRepository.findAllById(dto.getMenuIds());

        String imgUrl=existingThali.getImageUrl();
        String publicId=existingThali.getImagePublicId();

       Map<String, String> imageMap;
        try {
            if (file != null && !file.isEmpty()) {
                if (publicId != null && imgUrl !=null) {
                    imageUploadService.deleteImage(publicId);
                }
                imageMap = imageUploadService.uploadThaliImage(file);
                existingThali.setImageUrl(imageMap.get("url"));
                existingThali.setImagePublicId(imageMap.get("publicId"));
            } else {
                existingThali.setImageUrl(imgUrl);
                existingThali.setImagePublicId(publicId);
            }
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }
        existingThali.setThaliName(dto.getThaliName());
        existingThali.setThaliPrice(dto.getThaliPrice());
        existingThali.setMenus(menus);

        return mapToResponse(thaliRepository.save(existingThali));
    }

    @Override
    @Transactional
    public boolean deleteThali(Long id) {
        if (!thaliRepository.existsById(id)) return false;

        Thali thali = thaliRepository.findById(id).get();
        try {
            if (thali.getImagePublicId() != null) {
                imageUploadService.deleteImage(thali.getImagePublicId());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image from Cloudinary", e);
        }
        thaliRepository.deleteById(id);
        return true;
    }

    private ThaliResponseDto mapToResponse(Thali thali) {
        List<MenuResponseDto> menuDtos = thali.getMenus().stream()
                .map(m -> MenuResponseDto.builder()
                        .menuId(m.getMenuId())
                        .menuName(m.getMenuName())
                        .description(m.getDescription())
                        .price(m.getPrice())
                        .category(m.getMenuCategory())
                        // .available(m.isAvailable())
                        .hotelId(m.getHotel().getId())
                        .build())
                .collect(Collectors.toList());

        return ThaliResponseDto.builder()
                .thaliId(thali.getThaliId())
                .thaliName(thali.getThaliName())
                .thaliPrice(thali.getThaliPrice())
                .imageUrl(thali.getImageUrl())
                .imagePublicId(thali.getImagePublicId())
                .hotelId(thali.getHotel().getId())
                .menus(menuDtos)
                .build();
    }
}

