package com.hotel.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hotel.entity.dtos.menus.ThaliRequestDto;
import com.hotel.entity.dtos.menus.ThaliResponseDto;
import com.hotel.services.ThaliService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/thali")
@RequiredArgsConstructor
@CrossOrigin("*")
public class ThaliController {

    private final ThaliService thaliService;

    @PostMapping("/create-thali")
    public ResponseEntity<ThaliResponseDto> createThali(@RequestParam String thaliName,
                                                         @RequestParam double thaliPrice,
                                                         @RequestParam Long hotelId,
                                                         @RequestParam List<Long> menuIds,
                                                         @RequestParam(required = false) MultipartFile thaliImageFile) {
        ThaliRequestDto dto = new ThaliRequestDto();
        dto.setThaliName(thaliName);
        dto.setThaliPrice(thaliPrice);
        dto.setHotelId(hotelId);
        dto.setMenuIds(menuIds);
        return ResponseEntity.ok(thaliService.createThali(dto,thaliImageFile));
    }

    @GetMapping("/get-all-thalis")
    public ResponseEntity<List<ThaliResponseDto>> getThalisByHotel(@RequestParam Long hotelId) {
        List<ThaliResponseDto> thaliResponseDtos=thaliService.getThalisByHotel(hotelId);

        return !thaliResponseDtos.isEmpty()?
                ResponseEntity.ok(thaliResponseDtos):
                ResponseEntity.noContent().build();
    }

    @PutMapping("/update-thali")
    public ResponseEntity<ThaliResponseDto> updateThali(@RequestParam Long thaliId,
                                                        @RequestParam String thaliName,
                                                        @RequestParam double thaliPrice,
                                                        @RequestParam List<Long> menuIds,
                                                        @RequestParam(required = false) MultipartFile thaliImageFile) {
        ThaliRequestDto dto = new ThaliRequestDto();
        dto.setThaliName(thaliName);
        dto.setThaliPrice(thaliPrice);
        dto.setMenuIds(menuIds);
        return ResponseEntity.ok(thaliService.updateThali(thaliId, dto,thaliImageFile));
    }

    @DeleteMapping("/delete-thali")
    public ResponseEntity<?> deleteThali(@RequestParam Long thaliId) {
        return thaliService.deleteThali(thaliId) ?
                ResponseEntity.ok(true) :
                ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}

