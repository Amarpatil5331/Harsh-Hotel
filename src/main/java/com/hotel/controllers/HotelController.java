package com.hotel.controllers;


import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hotel.entity.AppUser;
import com.hotel.entity.Hotel;
import com.hotel.entity.dtos.hotel.HotelRequestDto;
import com.hotel.entity.dtos.hotel.HotelResponseDto;
import com.hotel.repositories.RoleRepository;
import com.hotel.services.AppUserService;
import com.hotel.services.HotelService;

import jakarta.transaction.Transactional;
import jakarta.websocket.server.PathParam;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/hotel")
@CrossOrigin("*")
@RequiredArgsConstructor
public class HotelController{

    private final HotelService hotelService;
    private final AppUserService appUserService;
    private final RoleRepository roleRepository;

    @PostMapping("/create-hotel")
    @Transactional
    public ResponseEntity<?> createHotel(
            @RequestParam("hotelName") String hotelName,
            @RequestParam("hotelAddress") String hotelAddress,
            @RequestParam("city") String city,
            @RequestParam("hotelContact") String hotelContact,
            @RequestParam("state") String state,
            @RequestParam("registrationNumber") String registrationNumber,
            @RequestParam("gst") String gst,
            @RequestParam("pincode") String pincode,
            @RequestParam("name") String name,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("contact") String contact,
            @RequestParam("email") String email,
            @RequestParam("userAddress") String userAddress,
            @RequestParam("salary") double salary,
            @RequestParam("role_id") Long roleId,
            @RequestParam(name = "hotelImage",required = false) MultipartFile hotelImage,
            @RequestParam(name = "userImage",required = false) MultipartFile userImage,
            @RequestParam(name = "aadharImage",required = false) MultipartFile aadharFile) throws IOException
    {
        HotelRequestDto requestDto=new HotelRequestDto();
        requestDto.setName(hotelName);
        requestDto.setAddress(hotelAddress);
        requestDto.setCity(city);
        requestDto.setHotelContact(hotelContact);
        requestDto.setState(state);
        requestDto.setRegistrationNumber(registrationNumber);
        requestDto.setGst(gst);
        requestDto.setPincode(pincode);
        Hotel savedHotel=hotelService.createHotel(requestDto,hotelImage);

        if(savedHotel==null){
            return ResponseEntity.badRequest().build();
        }

            AppUser user=new AppUser();
            user.setName(name);
            user.setUsername(username);
            user.setPassword(new BCryptPasswordEncoder().encode(password));
            user.setHotel(savedHotel);
            user.setDateOfJoining(LocalDate.now());
            user.setEmail(email);
            user.setContact(contact);
            user.setAddress(userAddress);
            user.setSalary(salary);

            if(roleRepository.findById(roleId).isEmpty()){
                hotelService.deleteHotelById(savedHotel.getId());
                return ResponseEntity.badRequest().build();
            }
            user.setRole(roleRepository.findById(roleId).get());
            user.setHotel(savedHotel);
            user.setIsDeleted(false);
            AppUser savedUser=appUserService.saveUser(user,userImage,aadharFile);

            if(savedUser ==null) {
                hotelService.deleteHotelById(savedHotel.getId());
                return ResponseEntity.badRequest().build();
            }
        HotelResponseDto dto = HotelResponseDto.builder()
                .id(savedHotel.getId())
                .name(savedHotel.getName())
                .address(savedUser.getAddress())
                .city(savedHotel.getCity())
                .hotelContact(savedHotel.getHotelContact())
                .state(savedHotel.getState())
                .registrationNumber(savedHotel.getRegistrationNumber())
                .gst(savedHotel.getGst())
                .pincode(savedHotel.getPincode())
                .isDeleted(savedHotel.isDeleted())
                .imageUrl(savedHotel.getImageUrl())
                .user(savedUser)
                .build();
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/get-all-hotels")
    public ResponseEntity<?> getAllHotels(){
        List<HotelResponseDto> hotels=hotelService.getAllHotels();
        return !hotels.isEmpty() ?
                ResponseEntity.ok(hotels):
                ResponseEntity.noContent().build();
    }

    @GetMapping("/get-hotel-by-id")
    public ResponseEntity<?> getHotelById(@PathParam("id") Long id){
        HotelResponseDto dto=hotelService.getHotelById(id);
        return dto!=null?
                ResponseEntity.ok(dto):
                ResponseEntity.noContent().build();
    }

    @PutMapping("/update-hotel")
    public ResponseEntity<?> updateHotel(
            @RequestParam("id") Long id,
            @RequestParam("hotelName") String hotelName,
            @RequestParam("hotelAddress") String hotelAddress,
            @RequestParam("city") String city,
            @RequestParam("hotelContact") String hotelContact,
            @RequestParam("state") String state,
            @RequestParam("registrationNumber") String registrationNumber,
            @RequestParam("gst") String gst,
            @RequestParam("pincode") String pincode,
            @RequestParam(name = "hotelImage",required = false) MultipartFile hotelImage)
    {

        HotelResponseDto responseDto=new HotelResponseDto();
        responseDto.setId(id);
        responseDto.setName(hotelName);
        responseDto.setAddress(hotelAddress);
        responseDto.setCity(city);
        responseDto.setHotelContact(hotelContact);
        responseDto.setState(state);
        responseDto.setRegistrationNumber(registrationNumber);
        responseDto.setGst(gst);
        responseDto.setPincode(pincode);

        Hotel hotel =hotelService.updateHotel(responseDto,hotelImage);
        if(hotel==null){
            return ResponseEntity.badRequest().build();
        }

        HotelResponseDto dto=HotelResponseDto.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .address(hotel.getAddress())
                .city(hotel.getCity())
                .hotelContact(hotel.getHotelContact())
                .state(hotel.getState())
                .registrationNumber(hotel.getRegistrationNumber())
                .gst(hotel.getGst())
                .pincode(hotel.getPincode())
                .isDeleted(hotel.isDeleted())
                .imageUrl(hotel.getImageUrl())
                .build();
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/delete-hotel")
    public ResponseEntity<?> deleteHotelById(@RequestParam("id") Long id){
        hotelService.deleteHotelById(id);
        return ResponseEntity.ok().build();
    }
}
