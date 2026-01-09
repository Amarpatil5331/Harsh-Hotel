package com.hotel.entity;

import com.hotel.entity.enums.ChairStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Chairs {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long chairId;

    @Enumerated(EnumType.STRING)
    private ChairStatus chairStatus;

    @Column(nullable = false)
    private String chairName;

    @ManyToOne
    @JoinColumn(name = "table_id", nullable = false)
    private Tables table;
}
