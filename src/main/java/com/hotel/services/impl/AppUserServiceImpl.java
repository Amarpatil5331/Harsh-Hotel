package com.hotel.services.impl;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hotel.entity.AppUser;
import com.hotel.entity.Payroll;
import com.hotel.entity.dtos.UserResponse;
import com.hotel.repositories.AdvancePaymentRepository;
import com.hotel.repositories.AppUserRepository;
import com.hotel.repositories.PayrollRepository;
import com.hotel.services.AppUserService;
import com.hotel.services.LeaveService;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    @Autowired private BCryptPasswordEncoder passwordEncoder;
    private final AppUserRepository userRepository;

    private final ImageUploadService imageUploadService;
    private final PayrollRepository payrollRepository;
    private final AdvancePaymentRepository advancePaymentRepository;
    private final LeaveService leaveService;

    @Override
    public AppUser saveUser(AppUser user, MultipartFile file, MultipartFile aadharFile) throws IOException {
        if(userRepository.existsByUsername(user.getUsername())){
            throw new RuntimeException("Username already exists");
        }

        // Handle regular image upload
        Map<String, String> imageMap;
        try {
            if (file != null && !file.isEmpty()) {
                imageMap = imageUploadService.uploadUserImage(file);
            } else {
                ClassPathResource imgFile = new ClassPathResource("static/superadmin2.jpg");
                imageMap = imageUploadService.uploadUserImage(imgFile.getFile());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        user.setImagePublicId(imageMap.get("publicId"));
        user.setImageUrl(imageMap.get("url"));

        // Handle Aadhar image upload
        if (aadharFile != null && !aadharFile.isEmpty()) {
            try {
                Map<String, String> aadharImageMap = imageUploadService.uploadAadharImage(aadharFile);
                user.setAadharImagePublicId(aadharImageMap.get("publicId"));
                user.setAadharImageUrl(aadharImageMap.get("url"));
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload Aadhar image: " + e.getMessage());
            }
        }

        AppUser savedUser = userRepository.save(user);

        // Auto-create payroll for the new user
        Payroll payroll = new Payroll();
        payroll.setUser(savedUser);
        payroll.setHotel(savedUser.getHotel());
        payroll.setTotalAdvancesTaken(0.0);
        payroll.setTotalAdvancesRemaining(0.0);
        payroll.setLastNetSalary(0.0);
        payrollRepository.save(payroll);

        return savedUser;
    }

    @Override
    public UserResponse getAppUser(Long id) {
        AppUser user = getSingleUser(id);
        if (user != null && !user.getIsDeleted()) {
            UserResponse response = new UserResponse();
            response.setId(user.getId());
            response.setName(user.getName());
            response.setAddress(user.getAddress());
            response.setUsername(user.getUsername());
            response.setPassword(user.getPassword()); // Optional: you may want to exclude this
            response.setContact(user.getContact());
            response.setImageUrl(user.getImageUrl());
            response.setAadharImageUrl(user.getAadharImageUrl());
            response.setEmail(user.getEmail());
            response.setDateOfJoining(user.getDateOfJoining());
            response.setSalary(user.getSalary());
//            response.setIsMultiFactor(user.getIsMultiFactor());
//            response.setIsUserLoggedIn(user.getIsUserLoggedIn());
            response.setIsDeleted(user.getIsDeleted());
            response.setCreatedAt(user.getCreatedAt());
            response.setUpdatedAt(user.getUpdatedAt());
            response.setRole(user.getRole());

            return response;
        }
        return null;
    }


    @Override
    public List<UserResponse> getAllAppUsers(Long id) {
        List<AppUser> users = userRepository.getAppUserByHotel_Id(id);
        return users.stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    private UserResponse mapToUserResponse(AppUser user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setAddress(user.getAddress());
        response.setUsername(user.getUsername());
        // response.setPassword(user.getPassword()); // Omit for security
        response.setContact(user.getContact());
        response.setEmail(user.getEmail());
        response.setDateOfJoining(user.getDateOfJoining());
        response.setImageUrl(user.getImageUrl());
        response.setAadharImageUrl(user.getAadharImageUrl());
        response.setSalary(user.getSalary());
//        response.setIsMultiFactor(user.getIsMultiFactor());
//        response.setIsUserLoggedIn(user.getIsUserLoggedIn());
        response.setIsDeleted(user.getIsDeleted());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        response.setRole(user.getRole());

        return response;
    }


    @Override
    public AppUser updateAppUser(AppUser user, MultipartFile file, MultipartFile aadharFile) throws IOException {
        AppUser appUser = getSingleUser(user.getId());

        if(appUser!=null){
            // Handle regular image upload
            Map<String, String> imageMap;
            try {
                if (file != null && !file.isEmpty()) {
                    imageUploadService.deleteImage(appUser.getImagePublicId());
                    imageMap = imageUploadService.uploadUserImage(file);
                    appUser.setImageUrl(imageMap.get("url"));
                    appUser.setImagePublicId(imageMap.get("publicId"));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            // Handle Aadhar image upload
            if (aadharFile != null && !aadharFile.isEmpty()) {
                try {
                    // Delete existing Aadhar image if present
                    if (appUser.getAadharImagePublicId() != null && !appUser.getAadharImagePublicId().isEmpty()) {
                        imageUploadService.deleteImage(appUser.getAadharImagePublicId());
                    }
                    Map<String, String> aadharImageMap = imageUploadService.uploadAadharImage(aadharFile);
                    appUser.setAadharImageUrl(aadharImageMap.get("url"));
                    appUser.setAadharImagePublicId(aadharImageMap.get("publicId"));
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload Aadhar image: " + e.getMessage());
                }
            }

            appUser.setName(user.getName());
            appUser.setContact(user.getContact());
            appUser.setAddress(user.getAddress());
            appUser.setUsername(user.getUsername());

            if (user.getPassword() != null && !user.getPassword().isEmpty()) {
                appUser.setPassword(passwordEncoder.encode(user.getPassword()));
            }
            appUser.setEmail(user.getEmail());
            appUser.setDateOfJoining(user.getDateOfJoining());
            appUser.setSalary(user.getSalary());

            return userRepository.save(appUser);
        }
        return null;
    }

    @Override
    @Transactional
    public boolean deleteUserById(Long userId) {
        Optional<AppUser> userOpt=userRepository.findById(userId);
        if(userOpt.isEmpty()){
            return false;
        }

        if(userOpt.get().getUsername().equals("superadmin.com") || userOpt.get().getRole().getRoleName().toLowerCase().equals("manager")){
            return false;
        }

        try{
            imageUploadService.deleteImage(userOpt.get().getImagePublicId());
            // Also delete Aadhar image if present
            if (userOpt.get().getAadharImagePublicId() != null && !userOpt.get().getAadharImagePublicId().isEmpty()) {
                imageUploadService.deleteImage(userOpt.get().getAadharImagePublicId());
            }
        }
        catch (Exception ex){
            throw new RuntimeException("User Image delete failed");
        }

        // Delete advance payments for this user
        List<com.hotel.entity.AdvancePayment> advancePayments = advancePaymentRepository.findByUserIdAndHotelIdOrderByCreatedAtDesc(userId, userOpt.get().getHotel().getId());
        advancePaymentRepository.deleteAll(advancePayments);

        // Delete payroll for this user
        payrollRepository.deleteByUserIdAndHotelId(userId, userOpt.get().getHotel().getId());

        // Delete all leave records for this user
        leaveService.deleteAllLeavesByUser(userId);

        userRepository.deleteById(userId);
        return true;
    }

//    @Override
//    public Boolean softDeleteUser(UUID id) {
//        AppUser user = getSingleUser(id);
//        if(user!=null && !user.getIsDeleted()){
//            user.setIsDeleted(true);
//            userRepository.save(user);
//            return true;
//        }
//        return false;
//    }

//    @Override
//    public Boolean retrieveUser(UUID id) {
//        AppUser user = getSingleUser(id);
//        if(user!=null && user.getIsDeleted()){
//            user.setIsDeleted(true);
//            userRepository.save(user);
//            return true;
//        }
//        return false;
//    }

//    @Override
//    public List<AppUser> getAllSoftDeletedUser() {
//        return userRepository.getAppUserByIsDeletedTrue();
//    }


    private AppUser getSingleUser(Long id){
        Optional<AppUser> user = userRepository.findById(id);
        return user.orElse(null);
    }
}
