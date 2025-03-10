package com.system.napas_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    @JoinColumn(name = "bank_id")
    private Bank bank;

    @Column(name = "account_id")
    private String accountId;

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

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<Transaction> transactionList;
}
