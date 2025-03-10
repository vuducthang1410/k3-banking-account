package com.system.core_banking_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.system.common_library.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Customer")
@Table(name = "tbl_customer")
@EqualsAndHashCode(callSuper = true)
public class Customer extends BaseEntity implements Serializable {

    @Column(name = "cif_code")
    private String cifCode;

    @Column(name = "full_name")
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Column(name = "birthday")
    private LocalDate birthday;

    @Column(name = "total_income")
    private BigDecimal totalIncome;

    @Column(name = "total_expenditure")
    private BigDecimal totalExpenditure;

    @Column(name = "last_transaction_date")
    private LocalDateTime lastTransactionDate;

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    @JsonBackReference
    private List<Account> accountList;
}
