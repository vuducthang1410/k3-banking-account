package com.system.account_service.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "saving_accounts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingAccount extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String accountId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "banking_account_id", nullable = false)
    private BankingAccount bankingAccount;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_common_id", nullable = false)
    private AccountCommons accountCommon;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id")
    private BranchBanking branch;

    @Column(columnDefinition="Decimal(15,2)", nullable = false)
    private BigDecimal balance;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "interest_rate_id", nullable = false)
    private InterestRates interestRate;

    private Date endDate;
}
