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

import com.hotel.entity.dtos.menus.MenuRequestDto;
import com.hotel.entity.dtos.menus.MenuResponseDto;
import com.hotel.entity.enums.MenuCategory;
import com.hotel.services.MenuService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
@CrossOrigin("*")
public class MenuController {

    private final MenuService menuService;

    @PostMapping("/create-menu")
    public ResponseEntity<MenuResponseDto> createMenu(@RequestParam String menuName,
                                                      @RequestParam String description,
                                                      @RequestParam double price,
                                                      @RequestParam String category,
                                                      @RequestParam Long hotelId,
                                                      @RequestParam(required = false) MultipartFile menuImageFile) {
        if(menuImageFile==null){
            System.out.println("IMage is null");
        }
        MenuRequestDto dto = new MenuRequestDto();
        dto.setMenuName(menuName);
        dto.setDescription(description);
        dto.setPrice(price);
        dto.setCategory(MenuCategory.valueOf(category.toUpperCase()));
        dto.setHotelId(hotelId);
        return ResponseEntity.ok(menuService.createMenu(dto,menuImageFile));
    }

    @GetMapping("/get-all-menus")
    public ResponseEntity<List<MenuResponseDto>> getMenusByHotel(@RequestParam Long hotelId) {
        List<MenuResponseDto> menuResponseDtos=menuService.getMenusByHotel(hotelId);

        return !menuResponseDtos.isEmpty()?
                ResponseEntity.ok(menuResponseDtos):
                ResponseEntity.noContent().build();
    }

    @PutMapping("/update-menu")
    public ResponseEntity<MenuResponseDto> updateMenu(@RequestParam Long menuId, 
                                                      @RequestParam String menuName,
                                                      @RequestParam String description,
                                                      @RequestParam double price,
                                                      @RequestParam String category,
                                                      @RequestParam(required = false) MultipartFile menuImageFile) {
        MenuRequestDto dto = new MenuRequestDto();
        dto.setMenuName(menuName);
        dto.setDescription(description);
        dto.setPrice(price);
        dto.setCategory(MenuCategory.valueOf(category.toUpperCase()));
        return ResponseEntity.ok(menuService.updateMenu(menuId, dto,menuImageFile));
    }

    @DeleteMapping("/delete-menu")
    public ResponseEntity<?> deleteMenu(@RequestParam Long menuId) {
        return menuService.deleteMenu(menuId) ?
                ResponseEntity.ok(true) :
                ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}

