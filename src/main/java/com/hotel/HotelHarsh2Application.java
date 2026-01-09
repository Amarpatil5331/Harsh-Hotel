package com.hotel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
@CrossOrigin("*")
public class HotelHarsh2Application {

    public static void main(String[] args) {
        SpringApplication.run(HotelHarsh2Application.class, args);
    }

    @GetMapping("/test")
    public String getMessage(){
        return "End-point for hotel harsh working properly...!";
    }
}
