package com.hotel.services.impl;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hotel.entity.Hotel;
import com.hotel.entity.dtos.hotel.HotelRequestDto;
import com.hotel.entity.dtos.hotel.HotelResponseDto;
import com.hotel.repositories.AdvancePaymentRepository;
import com.hotel.repositories.AppUserRepository;
import com.hotel.repositories.ChairRepository;
import com.hotel.repositories.HotelRepository;
import com.hotel.repositories.MenuRepository;
import com.hotel.repositories.OrderItemRepository;
import com.hotel.repositories.OrderRepository;
import com.hotel.repositories.PayrollRepository;
import com.hotel.repositories.TableOccupancyRepository;
import com.hotel.repositories.TableRepository;
import com.hotel.repositories.ThaliRepository;
import com.hotel.services.HotelService;
import com.hotel.services.LeaveService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service("hotelService")
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {
    private final HotelRepository hotelRepository;
    private final ImageUploadService imageUploadService;
    private final AppUserRepository appUserRepository;
    private final TableRepository tableRepository;
    private final ChairRepository chairRepository;
    private final MenuRepository menuRepository;
    private final ThaliRepository thaliRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableOccupancyRepository tableOccupancyRepository;
    private final AdvancePaymentRepository advancePaymentRepository;
    private final PayrollRepository payrollRepository;
    private final LeaveService leaveService;

    public Hotel createHotel(HotelRequestDto requestDto, MultipartFile file){
        if(hotelRepository.existsByRegistrationNumber(requestDto.getRegistrationNumber())){
            return null;
        }

        Map<String, String> imageMap;
        try {
            if (file != null && !file.isEmpty()) {
                imageMap = imageUploadService.uploadHotelImage(file);
            } else {
                ClassPathResource imgFile = new ClassPathResource("static/hotel.jpeg");
                imageMap = imageUploadService.uploadHotelImage(imgFile.getFile());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        Hotel hotel = new Hotel();
        hotel.setName(requestDto.getName());
        hotel.setAddress(requestDto.getAddress());
        hotel.setCity(requestDto.getCity());
        hotel.setHotelContact(requestDto.getHotelContact());
        hotel.setState(requestDto.getState());
        hotel.setRegistrationNumber(requestDto.getRegistrationNumber());
        hotel.setGst(requestDto.getGst());
        hotel.setPincode(requestDto.getPincode());
        hotel.setImageUrl(imageMap.get("url"));
        hotel.setImagePublicId(imageMap.get("publicId"));
        hotel.setDeleted(false);

        Hotel savedHotel = hotelRepository.save(hotel);

        return savedHotel;
    }

    public List<HotelResponseDto> getAllHotels(){
        List<Hotel> hotels=hotelRepository.findAll();

        return hotels.stream().map(hotel -> HotelResponseDto.builder()
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
                .build()).collect(Collectors.toList());
    }

    public HotelResponseDto getHotelById(Long id){
        Optional<Hotel> hotelOpt=hotelRepository.findById(id);
        if(hotelOpt.isEmpty()){
            return null;
        }

        Hotel hotel=hotelOpt.get();
        return HotelResponseDto.builder()
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
    }

    @Transactional
    public Hotel updateHotel(HotelResponseDto responseDto,MultipartFile file){
        Hotel existingHotel = hotelRepository.findById(responseDto.getId())
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        if (hotelRepository.existsByRegistrationNumberAndIdNot(
                responseDto.getRegistrationNumber(), responseDto.getId())) {
            throw new RuntimeException("Registration number already in use");
        }

        String imgUrl = existingHotel.getImageUrl();
        String publicId = existingHotel.getImagePublicId();

        Map<String, String> imageMap;
        try {
            if (file != null && !file.isEmpty()) {
                if (publicId != null && imgUrl !=null) {
                    imageUploadService.deleteImage(publicId);
                }
                imageMap = imageUploadService.uploadHotelImage(file);
                existingHotel.setImageUrl(imageMap.get("url"));
                existingHotel.setImagePublicId(imageMap.get("publicId"));
            } else {
                existingHotel.setImageUrl(imgUrl);
                existingHotel.setImagePublicId(publicId);
            }
        } catch (IOException e) {
            throw new RuntimeException("Image upload failed", e);
        }

        existingHotel.setName(responseDto.getName());
        existingHotel.setAddress(responseDto.getAddress());
        existingHotel.setCity(responseDto.getCity());
        existingHotel.setHotelContact(responseDto.getHotelContact());
        existingHotel.setState(responseDto.getState());
        existingHotel.setRegistrationNumber(responseDto.getRegistrationNumber());
        existingHotel.setGst(responseDto.getGst());
        existingHotel.setPincode(responseDto.getPincode());
        existingHotel.setDeleted(false);

        return hotelRepository.save(existingHotel);
    }

    @Transactional
    public void deleteHotelById(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hotel not found"));

        // Delete all related data in the correct order to avoid foreign key constraints

        // 1. Delete table occupancy records first (they reference orders)
        List<com.hotel.entity.Order> orders = orderRepository.findByHotelIdAndStatus(id, null);
        for (com.hotel.entity.Order order : orders) {
            List<com.hotel.entity.TableOccupancy> occupancies = tableOccupancyRepository.findByOrder(order);
            tableOccupancyRepository.deleteAll(occupancies);
        }

        // 2. Delete order items (they reference orders)
        for (com.hotel.entity.Order order : orders) {
            List<com.hotel.entity.OrderItem> orderItems = orderItemRepository.findByOrder(order);
            orderItemRepository.deleteAll(orderItems);
        }

        // 3. Delete orders
        orderRepository.deleteAll(orders);

        // 4. Delete chairs (they reference tables)
        List<com.hotel.entity.Tables> tables = tableRepository.getTablesByHotel_Id(id);
        for (com.hotel.entity.Tables table : tables) {
            List<com.hotel.entity.Chairs> chairs = table.getChairs();
            if (chairs != null && !chairs.isEmpty()) {
                chairRepository.deleteAll(chairs);
            }
        }

        // 5. Delete tables
        tableRepository.deleteAll(tables);

        // 6. Delete menus and thalis
        List<com.hotel.entity.Menu> menus = menuRepository.findByHotel_Id(id);
        menuRepository.deleteAll(menus);

        List<com.hotel.entity.Thali> thalis = thaliRepository.findByHotel_Id(id);
        thaliRepository.deleteAll(thalis);

        // 7. Delete advance payments and payrolls associated with the hotel's users
        List<com.hotel.entity.AdvancePayment> advancePayments = advancePaymentRepository.findByHotelId(id);
        advancePaymentRepository.deleteAll(advancePayments);

        List<com.hotel.entity.Payroll> payrolls = payrollRepository.findByHotelId(id);
        payrollRepository.deleteAll(payrolls);

        // 8. Delete leaves associated with the hotel's users
        List<com.hotel.entity.AppUser> users = appUserRepository.getAppUserByHotel_Id(id);
        for (com.hotel.entity.AppUser user : users) {
            leaveService.deleteAllLeavesByUser(user.getId());
        }

        // 9. Delete users associated with the hotel
        appUserRepository.deleteAll(users);

        // 10. Delete hotel image from Cloudinary
        if (hotel.getImagePublicId() != null) {
            try {
                imageUploadService.deleteImage(hotel.getImagePublicId());
            } catch (IOException e) {
                throw new RuntimeException("Failed to delete image from Cloudinary", e);
            }
        }

        // 11. Finally delete the hotel
        hotelRepository.deleteById(id);
    }
}
