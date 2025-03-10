package com.system.account_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "loan_accounts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanAccount extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String accountId;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_common_id", nullable = false)
    private AccountCommons accountCommon;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "banking_account_id", nullable = false)
    private BankingAccount bankingAccount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id")
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
