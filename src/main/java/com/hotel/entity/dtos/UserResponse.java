package com.hotel.entity.dtos;


import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hotel.entity.Role;

import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String address;
    private String username;
    private String password;
    private String contact;
    private String email;
    private String imageUrl;
    private String aadharImageUrl;
    private LocalDate dateOfJoining;
    private double salary;
//    private Boolean isMultiFactor;
//    private Boolean isUserLoggedIn;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Role role;
}
