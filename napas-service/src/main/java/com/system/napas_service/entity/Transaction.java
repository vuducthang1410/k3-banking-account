package com.system.napas_service.entity;

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
@Entity(name = "Transaction")
@Table(name = "tbl_transaction")
@EqualsAndHashCode(callSuper = true)
public class Transaction extends BaseEntity implements Serializable {

    @ManyToOne
    @JsonManagedReference
    @JoinColumn(name = "account_id")
    private Account account;

    @Column(name = "napas_code")
    private String napasCode;

    @Column(name = "sender_account")
    private String senderAccount;

    @Column(name = "receiver_account")
    private String receiverAccount;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "previous_balance")
    private BigDecimal previousBalance;

    @Column(name = "current_balance")
    private BigDecimal currentBalance;

    @Column(name = "available_balance")
    private BigDecimal availableBalance;

    @Column(name = "reference_code")
    private String referenceCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction")
    private Direction direction;

    @Column(name = "note")
    private String note;
}
