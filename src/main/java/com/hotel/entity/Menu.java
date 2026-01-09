package com.hotel.entity;

import com.hotel.entity.enums.MenuCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuId;

    private String menuName;
    private String description;
    private double price;

    @Enumerated(EnumType.STRING)
    private MenuCategory menuCategory;

    // private boolean available=true;

    private String imageUrl;
    private String imagePublicId;

    @ManyToOne
    @JoinColumn(name = "hotel_id",nullable = false)
    private Hotel hotel;
}
