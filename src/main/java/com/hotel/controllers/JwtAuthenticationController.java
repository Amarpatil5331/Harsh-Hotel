package com.hotel.controllers;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hotel.config.jwt.JwtUtils;
import com.hotel.entity.AppUser;
import com.hotel.entity.Permissions;
import com.hotel.entity.Privilege;
import com.hotel.entity.Role;
import com.hotel.entity.dtos.JwtRequest;
import com.hotel.entity.dtos.JwtResponse;
import com.hotel.entity.dtos.PermissionDto;
import com.hotel.entity.dtos.RoleDto;
import com.hotel.repositories.AppUserRepository;
import com.hotel.repositories.HotelRepository;
import com.hotel.repositories.PermissionRepository;
import com.hotel.repositories.RoleRepository;
import com.hotel.services.RoleService;
import com.hotel.services.impl.ImageUploadService;
import com.hotel.services.jwt.MyUserDetailsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/service")
@CrossOrigin("*")
public class JwtAuthenticationController {

    @Autowired private BCryptPasswordEncoder passwordEncoder;
    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleService roleService;
    private final AuthenticationManager authenticationManager;
    private final MyUserDetailsService userDetailsService;
    private final JwtUtils jwtUtils;
    private final ImageUploadService imageUploadService;
    private final HotelRepository hotelRepository;


    @PostConstruct
    public void createAdmin() throws IOException {
        Optional<AppUser> optionalUser = userRepository.getMyUserByUsername("superadmin.com");
        if (optionalUser.isEmpty()) {

//            Optional<Role> optionalRole = roleRepository.findByRoleName("SUPER_ADMIN");

            Role savedRole = roleRepository.findByRoleName("SUPER_ADMIN")
                    .orElseGet(() -> {
                        Role role = new Role();
                        role.setRoleName("SUPER_ADMIN");
                        role.setRoleDescription("This is super admin role");
                        return roleRepository.save(role);
                    });

            if (permissionRepository.getPermissionsByRole(savedRole).isEmpty()) {
                Privilege privilege = new Privilege();
                privilege.setWritePermission("WRITE");
                privilege.setReadPermission("READ");
                privilege.setDeletePermission("DELETE");
                privilege.setUpdatePermission("UPDATE");

                Permissions permissions = new Permissions();
                permissions.setUserPermission("ALL_PERMISSIONS");
                permissions.setRole(savedRole);
                permissions.setPrivilege(privilege);
                roleService.createPermissions(List.of(permissions));
            }

            AppUser user = new AppUser();
            user.setName("harsh");
            user.setUsername("superadmin.com");
            user.setContact("1234567890");
            user.setAddress("");
            user.setEmail("-");
            user.setDateOfJoining(null);
            user.setHotel(null);
            user.setSalary(0);
            user.setIsDeleted(false);
            user.setRole(savedRole);
            user.setPassword(passwordEncoder.encode("superadmin@123"));

            ClassPathResource imgFile = new ClassPathResource("static/superadmin2.jpg");
            Map<String , String> imageMap= imageUploadService.uploadUserImage(imgFile.getFile());

//            String bucketName = "santosh-roofing-resource-bucket";
//            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
//                    .bucket(bucketName)
//                    .key(imageName)
//                    .contentType("image/jpeg")
//                    .build();
//
//            s3Client.putObject(putObjectRequest, software.amazon.awssdk.core.sync.RequestBody.fromBytes(imageBytes));
//            String imageUrl = String.format("https://%s.s3.amazonaws.com/%s", bucketName, imageName);
//            user.setIsMultiFactor(false);
            user.setImageUrl(imageMap.get("url"));
            user.setImagePublicId(imageMap.get("publicId"));
            user.setIsDeleted(false);
//            user.setIsUserLoggedIn(false);
            userRepository.save(user);
        }
    }

        @PostMapping("/login")
        public ResponseEntity<?> loginUser(@RequestBody JwtRequest jwtRequest){
            System.out.println(jwtRequest.getUsername()+"--->"+jwtRequest.getPassword());
            try {
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(jwtRequest.getUsername(), jwtRequest.getPassword())
                );
            } catch (BadCredentialsException e) {
                throw new BadCredentialsException("Incorrect Username or Password.");
            }
            UserDetails userDetails = userDetailsService.loadUserByUsername(jwtRequest.getUsername());
            System.out.println("DB password: " + userDetails.getPassword());
            System.out.println("Raw password: " + jwtRequest.getPassword());
            Optional<AppUser> optionalUser = userRepository.getMyUserByUsername(userDetails.getUsername());
            String token = jwtUtils.generateToken(userDetails.getUsername());
            JwtResponse response = new JwtResponse();

            if (optionalUser.isPresent()) {
    //            optionalUser.get().setIsUserLoggedIn(true);
                optionalUser.get().setUpdatedAt(LocalDateTime.now());
                userRepository.save(optionalUser.get());

                response.setIsLoggedIn(true);
                response.setJwtToken(token);
                Role role= optionalUser.get().getRole();

                RoleDto roleDto=new RoleDto();

                roleDto.setRoleId(role.getId());
                roleDto.setRoleName(role.getRoleName());
                roleDto.setRoleDescription(role.getRoleDescription());

                List<PermissionDto> permissionDtos=new ArrayList<>();
                for(Permissions permission:role.getPermissions()){
                    PermissionDto permissionDto=new PermissionDto();
                    List<String> privileges=new ArrayList<>();
                    permissionDto.setUserPermission(permission.getUserPermission());
                    privileges.add(permission.getPrivilege().getReadPermission());
                    privileges.add(permission.getPrivilege().getWritePermission());
                    privileges.add(permission.getPrivilege().getUpdatePermission());
                    privileges.add(permission.getPrivilege().getDeletePermission());

                    permissionDto.setPrivileges(privileges);
                    permissionDtos.add(permissionDto);
                }

                roleDto.setPermissions(permissionDtos);

                response.setRole(roleDto);
                AppUser user = optionalUser.get();
                response.setUserId(user.getId());
                response.setUsername(user.getUsername());
                response.setImageUrl(user.getImageUrl());

                if(user.getHotel() !=null){
                    response.setHotel(hotelRepository.findById(user.getHotel().getId()).get());
                }

                return ResponseEntity.status(HttpStatus.OK).body(response);
            }
            return null;
        }


    @PostMapping("/is-token-expired")
    public ResponseEntity<?> isTokenExpired(@RequestBody JwtResponse jwtResponse){
        try {
            return ResponseEntity.status(HttpStatus.OK).body(jwtUtils.isTokenExpired(jwtResponse.getJwtToken()));
        }catch (Exception e){
            Optional<AppUser> user = userRepository.getMyUserByUsername(jwtResponse.getUsername());
            if(user.isPresent()){
                user.get().setUpdatedAt(LocalDateTime.now());
//                user.get().setIsUserLoggedIn(false);
                AppUser save = userRepository.save(user.get());
                return ResponseEntity.ok(save);
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }



    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestParam String username) {
        Optional<AppUser> optionalUser = userRepository.getMyUserByUsername(username);
        if (optionalUser.isPresent()) {
            optionalUser.get().setUpdatedAt(LocalDateTime.now());
//            optionalUser.get().setIsUserLoggedIn(false);
            AppUser save = userRepository.save(optionalUser.get());
            return ResponseEntity.ok(save);
        }
        return ResponseEntity.ok(false);
    }
}
