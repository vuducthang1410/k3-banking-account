package com.system.account_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "banking_accounts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankingAccount extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String accountId;

    @Column(nullable = false)
    private String nickName;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_common_id", nullable = false)
    private AccountCommons accountCommon;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id", nullable = false)
    private BranchBanking branch;

    @Column(nullable = false)
    private BigDecimal balance;

    @PrePersist
    protected void onCreate() {
        if(balance == null) {
            balance = BigDecimal.ZERO;
        }
    }
}
