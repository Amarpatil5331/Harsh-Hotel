package com.hotel.controllers;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hotel.entity.AppUser;
import com.hotel.entity.Role;
import com.hotel.entity.dtos.RoleDto;
import com.hotel.entity.dtos.UserResponse;
import com.hotel.repositories.HotelRepository;
import com.hotel.repositories.RoleRepository;
import com.hotel.services.AppUserService;
import com.hotel.services.RoleService;

import lombok.AllArgsConstructor;


@RestController
@RequestMapping("/api/user")
@AllArgsConstructor
@CrossOrigin("*")
public class AppUserController {

    @Autowired private BCryptPasswordEncoder passwordEncoder;
    private final AppUserService appUserService;

    private final RoleService roleService;
    private final RoleRepository roleRepository;
    private final HotelRepository hotelRepository;

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestParam String name,
                                        @RequestParam String address,
                                        @RequestParam String username,
                                        @RequestParam String password,
                                        @RequestParam String contact,
                                        @RequestParam String email,
                                        @RequestParam(required = false) LocalDate dateOfJoining,
                                        @RequestParam double salary,
                                        @RequestParam(name = "image",required = false)MultipartFile imageFile,
                                        @RequestParam(name = "aadharImage",required = false)MultipartFile aadharFile,
                                        @RequestParam Long role_id,
                                        @RequestParam Long hotel_id) throws IOException {
        AppUser user= new AppUser();
        user.setName(name);
        user.setAddress(address);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setContact(contact);
        user.setEmail(email);
        user.setDateOfJoining(dateOfJoining);
        user.setSalary(salary);
//        user.setIsUserLoggedIn(false);
        user.setIsDeleted(false);
//        user.setIsMultiFactor(false);
        user.setRole(roleRepository.findById(role_id).get());
        user.setHotel(hotelRepository.findById(hotel_id).get());

        AppUser appUser = appUserService.saveUser(user, imageFile, aadharFile);
        if(appUser!=null){
            return ResponseEntity.ok(appUser);
        }
        return ResponseEntity.ok(null);
    }

    @GetMapping("/get-user")
    public ResponseEntity<?> getUser(@RequestParam Long id){
        return ResponseEntity.ok(appUserService.getAppUser(id));
    }

    @GetMapping("/get-all-users")
    public ResponseEntity<?> getAllUser(@RequestParam Long hotelId){
        List<UserResponse> allAppUsers = appUserService.getAllAppUsers(hotelId);
        if(allAppUsers!=null){
            return ResponseEntity.ok(allAppUsers);
        }
        return ResponseEntity.ok(null);
    }

//    @DeleteMapping("/soft-delete-user")
//    public ResponseEntity<?> softDeleteUser(@RequestParam UUID id){
//        return ResponseEntity.ok(appUserService.softDeleteUser(id));
//    }
//    @PutMapping("/retrieve-user")
//    public ResponseEntity<?> retrieveUser(@RequestParam UUID id){
//        return ResponseEntity.ok(appUserService.retrieveUser(id));
//    }
//    @GetMapping("/get-all-deleted-user")
//    public ResponseEntity<?> getDeletedUsers(){
//        List<AppUser> user = appUserService.getAllSoftDeletedUser();
//        if(user!=null){
//            return ResponseEntity.ok(user);
//        }
//        return ResponseEntity.ok(null);
//    }

    @PutMapping("/update-user")
    public ResponseEntity<?> updateUser(@RequestParam Long id,
                                        @RequestParam String name,
                                        @RequestParam String address,
                                        @RequestParam String username,
                                        @RequestParam(required = false) String password,
                                        @RequestParam String contact,
                                        @RequestParam String email,
                                        @RequestParam(required = false) LocalDate dateOfJoining,
                                        @RequestParam(required = false) Double salary,
//                                        @RequestParam Boolean isDelated,
                                        @RequestParam(name = "file",required = false)MultipartFile imageFile,
                                        @RequestParam(name = "aadharImage",required = false)MultipartFile aadharFile,
                                        @RequestParam Long role_id) throws IOException {

        AppUser appUser = new AppUser();
        appUser.setId(id);
        appUser.setName(name);
        appUser.setAddress(address);
        appUser.setUsername(username);
        appUser.setPassword(password);
        appUser.setContact(contact);
        appUser.setEmail(email);
        if(dateOfJoining!=null){
            appUser.setDateOfJoining(dateOfJoining);
        }
        if(salary!=null){
            appUser.setSalary(salary);
        }
        appUser.setRole(roleRepository.findById(role_id).get());
        AppUser user = appUserService.updateAppUser(appUser, imageFile, aadharFile);
        if(user!=null){
            return ResponseEntity.ok(user);
        }
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<?> deleteUser(@RequestParam Long id){
        boolean result=appUserService.deleteUserById(id);
        return result?
                ResponseEntity.ok(true):
                ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @PostMapping("/create-role")
    public ResponseEntity<?> createRole(@RequestBody RoleDto roleDto){
        Role role = roleService.createRole(roleDto);
        if(role!=null){
            return ResponseEntity.ok(role);
        }
        return ResponseEntity.ok(null);
    }

    @GetMapping("/get-all-roles")
    public ResponseEntity<?> getAllRole(){
        return ResponseEntity.ok(roleService.getAllRoles());
    }

    @GetMapping("/get-role-by-role-name")
    public ResponseEntity<?> getRole(@RequestParam String roleName){
        try{
            return ResponseEntity.status(HttpStatus.OK).body(roleService.getRoleByRoleName(roleName));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }
    }

    @PutMapping("/update-role")
    public ResponseEntity<?> updateRole(@RequestBody RoleDto roleDto) {
        return ResponseEntity.status(HttpStatus.OK).body(roleService.updateRole(roleDto));
    }
    /**
     *    Deletes a role.
     *       @param id The ID of the role to be deleted.
     *       @return {@link ResponseEntity} containing success or failure response.
     *
     */
    @DeleteMapping("/delete-role")
    public ResponseEntity<?> deleteRole(@RequestParam Long id){
        boolean result= roleService.deleteRole(id);
        return result?
                ResponseEntity.ok(true):
                ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

}
