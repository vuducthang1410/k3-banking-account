package com.system.account_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_accounts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditAccount extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String accountId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "banking_account_id", nullable = false)
    private BankingAccount bankingAccount;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_common_id", nullable = false)
    private AccountCommons accountCommon;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "interest_rate_id", nullable = false)
    private InterestRates interestRate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id")
    private BranchBanking branch;

    @Column(nullable = false)
    private BigDecimal creditLimit;

    @Column(nullable = false)
    private BigDecimal debtBalance;

    @Column(nullable = false)
    private Integer billingCycle;

    private LocalDateTime lastPaymentDate;

    @Transient
    @Getter(AccessLevel.NONE)
    private BigDecimal availableBalance;

    @PrePersist
    public void onCreate() {
        if(creditLimit == null) {
            creditLimit = BigDecimal.ZERO;
        }

        if(debtBalance == null) {
            debtBalance = BigDecimal.ZERO;
        }

        if(billingCycle == null) {
            billingCycle = 30;
        }
    }

    public BigDecimal getAvailableBalance() {
        return creditLimit.subtract(debtBalance);
    }
}
