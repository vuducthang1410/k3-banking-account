package com.system.transaction_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "PaymentTransaction")
@Table(name = "tbl_payment_transaction")
@EqualsAndHashCode(callSuper = true)
public class PaymentTransaction extends Transaction implements Serializable {

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "provider_name")
    private String providerName;

    @Column(name = "provider_code")
    private String providerCode;

    @Column(name = "invoice_number")
    private String invoiceNumber;

    @Column(name = "promotion_code")
    private String promotionCode;

    @Column(name = "qr_code")
    private String qrCode;

    @Column(name = "payment_code")
    private String paymentCode;

    @Column(name = "issue_date")
    private LocalDateTime issueDate;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "state")
    private Boolean state;
}
