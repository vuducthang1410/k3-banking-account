package com.system.core_banking_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.system.common_library.enums.AccountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Account")
@Table(name = "tbl_account")
@EqualsAndHashCode(callSuper = true)
public class Account extends BaseEntity implements Serializable {

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "account_number")
    private String accountNumber;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "available_balance")
    private BigDecimal availableBalance;

    @Column(name = "total_income")
    private BigDecimal totalIncome;

    @Column(name = "total_expenditure")
    private BigDecimal totalExpenditure;

    @Column(name = "currency")
    private String currency;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AccountType type;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<Transaction> transactionList;
}
