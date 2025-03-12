package com.system.account_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "credit_profiles")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditProfiles extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String customerId;

    private String company;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private BigDecimal income;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "credit_account_id")
    private CreditAccount creditAccount;
}
