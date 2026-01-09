package com.hotel.services;


import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hotel.entity.AppUser;
import com.hotel.entity.dtos.UserResponse;

public interface AppUserService {

    AppUser saveUser(AppUser user, MultipartFile imageFile, MultipartFile aadharFile) throws IOException;

    UserResponse getAppUser(Long id);

    List<UserResponse> getAllAppUsers(Long id);

    AppUser updateAppUser(AppUser user, MultipartFile imageFile, MultipartFile aadharFile) throws IOException;

    boolean deleteUserById(Long userId);



//    Boolean softDeleteUser(Long id);
//
//    Boolean retrieveUser(Long id);
//
//    List<AppUser> getAllSoftDeletedUser();


}
