package com.system.transaction_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.system.common_library.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Transaction")
@Table(name = "tbl_transaction",
        indexes = {
                @Index(name = "idx_sender_receiver_name", columnList = "sender_account_name, receiver_account_name, note")
        }
)
@EqualsAndHashCode(callSuper = true)
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Transaction extends BaseEntity implements Serializable {

    @Column(name = "cif_code")
    private String cifCode;

    @Column(name = "sender_account_id")
    private String senderAccountId;

    @Column(name = "sender_account")
    private String senderAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_account_type")
    private AccountType senderAccountType;

    @Column(name = "sender_account_name")
    private String senderAccountName;

    @Column(name = "receiver_account_id")
    private String receiverAccountId;

    @Column(name = "receiver_account")
    private String receiverAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "receiver_account_type")
    private AccountType receiverAccountType;

    @Column(name = "receiver_account_name")
    private String receiverAccountName;

    @Column(name = "reference_code")
    private String referenceCode;

    @Column(name = "core_rollback_code")
    private String coreRollbackCode;

    @Column(name = "napas_rollback_code")
    private String napasRollbackCode;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "fee")
    private BigDecimal fee;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_payer")
    private FeePayer feePayer;

    @Column(name = "note")
    private String note;

    @Column(name = "otp_code")
    private String otpCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "initiator")
    private Initiator initiator;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type")
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "method")
    private Method method;

    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonBackReference
    private List<TransactionDetail> transactionDetailList;

    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonBackReference
    private List<TransactionState> transactionStateList;
}
