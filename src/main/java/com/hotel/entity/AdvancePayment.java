package com.hotel.entity;

import com.hotel.entity.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "advance_payments")
@EntityListeners(AuditingEntityListener.class)
public class AdvancePayment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @Column(nullable = false)
    private double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(length = 500)
    private String description;

    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private double remainingBalanceAfterTransaction;

    private Integer month;
    private Integer year;
}
