package com.system.transaction_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.system.common_library.enums.Direction;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "TransactionDetail")
@Table(name = "tbl_transaction_detail")
@EqualsAndHashCode(callSuper = true)
public class TransactionDetail extends BaseEntity implements Serializable {

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "transaction_id")
    private Transaction transaction;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "account")
    private String account;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "fee")
    private BigDecimal fee;

    @Column(name = "net_amount")
    private BigDecimal netAmount;

    @Column(name = "previous_balance")
    private BigDecimal previousBalance;

    @Column(name = "current_balance")
    private BigDecimal currentBalance;

    @Column(name = "available_balance")
    private BigDecimal availableBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction")
    private Direction direction;
}
