package com.hotel.entity.dtos.hotel;

import lombok.Data;

@Data
public class HotelRequestDto {
    private String name;
    private String address;
    private String city;
    private String hotelContact;
    private String state;
    private String registrationNumber;
    private String gst;
    private String pincode;
}
