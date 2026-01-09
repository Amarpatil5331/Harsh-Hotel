package com.hotel.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Permissions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;
    private String userPermission;

    @ManyToOne
    @JsonBackReference
    private Role role;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "privilege_id")
    private Privilege privilege;
}
