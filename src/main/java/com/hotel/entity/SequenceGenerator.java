package com.hotel.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SequenceGenerator {
    @Id
    private String name;   // e.g., "USER", "ORDER"
    private Long currentValue;
}

