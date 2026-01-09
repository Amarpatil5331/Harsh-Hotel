package com.hotel.entity;

import java.util.List;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderTable {
    private String tableName;

    private List<String> chairs;
}
