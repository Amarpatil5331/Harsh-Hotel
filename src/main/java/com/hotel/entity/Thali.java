package com.hotel.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Thali {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long thaliId;

    private String thaliName;
    private double thaliPrice;

    private String imageUrl;
    private String imagePublicId;

    @ManyToOne
    @JoinColumn(name = "hotel_id",nullable = false)
    private Hotel hotel;

    @ManyToMany
    @JoinTable(
            name = "thali_menus",
            joinColumns = @JoinColumn(name = "thali_id"),
            inverseJoinColumns = @JoinColumn(name = "menu_id")
    )
    private List<Menu> menus=new ArrayList<>();
}
