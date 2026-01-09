package com.hotel.entity.dtos.hotel;

import com.hotel.entity.AppUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class HotelResponseDto {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String hotelContact;
    private String state;
    private String registrationNumber;
    private String gst;
    private String pincode;
    private boolean isDeleted;
    private String imageUrl;
    private AppUser user;
}
