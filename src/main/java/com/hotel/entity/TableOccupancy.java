package com.hotel.entity;

import com.hotel.entity.enums.OccupancyStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "table_occupancy")
public class TableOccupancy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long occupancyId;

    @Column(nullable = false)
    private String hotelName;

    @Column(nullable = false)
    private Long tableId;

    @Column
    private Long chairId; // nullable if full table booked

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OccupancyStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
