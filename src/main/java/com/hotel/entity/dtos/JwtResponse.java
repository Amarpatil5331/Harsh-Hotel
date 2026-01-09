package com.hotel.entity.dtos;

import com.hotel.entity.Hotel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JwtResponse {
    private Long userId;
    private String username;
    private Boolean isLoggedIn;
    private String jwtToken;
    private RoleDto role;
    private String imageUrl;
    private Hotel hotel;
}
