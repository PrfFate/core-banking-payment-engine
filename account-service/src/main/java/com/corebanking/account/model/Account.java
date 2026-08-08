package com.corebanking.account.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency; // TRY, USD, EUR

    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, FROZEN, CLOSED

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (balance == null) balance = BigDecimal.ZERO;
        if (currency == null) currency = "TRY";
        if (status == null) status = "ACTIVE";
    }
}
