package com.hotel.services.impl;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import lombok.RequiredArgsConstructor;

@Service("imageUploadService")
@RequiredArgsConstructor
public class ImageUploadService {
    private final Cloudinary cloudinary;

    //====================================================================================

    @SuppressWarnings("rawtypes")
    public Map<String, String> uploadHotelImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "hotel_images"));

        return Map.of(
                "url", uploadResult.get("secure_url").toString(),
                "publicId", uploadResult.get("public_id").toString()
        );
    }

    @SuppressWarnings("rawtypes")
    public Map<String, String> uploadHotelImage(File file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file,
                ObjectUtils.asMap("folder", "hotel_images"));

        return Map.of(
                "url", uploadResult.get("secure_url").toString(),
                "publicId", uploadResult.get("public_id").toString()
        );
    }

    //====================================================================================


    @SuppressWarnings("rawtypes")
    public Map<String,String> uploadUserImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "user_images")); // optional: store in folder

        return Map.of(
                "url", uploadResult.get("secure_url").toString(),
                "publicId", uploadResult.get("public_id").toString()
        ); // Cloudinary returns a public image URL
    }

    // Overload for File
    @SuppressWarnings("rawtypes")
    public Map<String,String> uploadUserImage(File file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file,
                ObjectUtils.asMap("folder", "user_images"));
        return Map.of(
                "url", uploadResult.get("secure_url").toString(),
                "publicId", uploadResult.get("public_id").toString()
        );
    }

    //====================================================================================

    @SuppressWarnings("rawtypes")
    public Map<String,String> uploadMenuImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "menu_images")); // optional: store in folder

        return Map.of(
                "url", uploadResult.get("secure_url").toString(),
                "publicId", uploadResult.get("public_id").toString()
        ); // Cloudinary returns a public image URL
    }

    // Overload for File
    @SuppressWarnings("rawtypes")
    public Map<String,String> uploadMenuImage(File file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file,
                ObjectUtils.asMap("folder", "menu_images"));
        return Map.of(
                "url", uploadResult.get("secure_url").toString(),
                "publicId", uploadResult.get("public_id").toString()
        );
    }    

    //====================================================================================

    @SuppressWarnings("rawtypes")
    public Map<String,String> uploadThaliImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "thali_images")); // optional: store in folder

        return Map.of(
                "url", uploadResult.get("secure_url").toString(),
                "publicId", uploadResult.get("public_id").toString()
        ); // Cloudinary returns a public image URL
    }

    // Overload for File
    @SuppressWarnings("rawtypes")
    public Map<String,String> uploadThaliImage(File file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file,
                ObjectUtils.asMap("folder", "thali_images"));
        return Map.of(
                "url", uploadResult.get("secure_url").toString(),
                "publicId", uploadResult.get("public_id").toString()
        );
    }

    //====================================================================================

    @SuppressWarnings("rawtypes")
    public Map<String,String> uploadAadharImage(MultipartFile file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap("folder", "aadhar_images")); // optional: store in folder

        return Map.of(
                "url", uploadResult.get("secure_url").toString(),
                "publicId", uploadResult.get("public_id").toString()
        ); // Cloudinary returns a public image URL
    }

    // Overload for File
    @SuppressWarnings("rawtypes")
    public Map<String,String> uploadAadharImage(File file) throws IOException {
        Map uploadResult = cloudinary.uploader().upload(file,
                ObjectUtils.asMap("folder", "aadhar_images"));
        return Map.of(
                "url", uploadResult.get("secure_url").toString(),
                "publicId", uploadResult.get("public_id").toString()
        );
    }

    
    public void deleteImage(String publicId) throws IOException {
        if (publicId != null && !publicId.isEmpty()) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }
}
