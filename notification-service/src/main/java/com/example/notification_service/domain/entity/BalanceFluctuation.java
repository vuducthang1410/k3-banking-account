package com.example.notification_service.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "balance_fluctuation")
public class BalanceFluctuation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false , name = "customer_cif")
    private String customerCIF;
    @Column(nullable = false , name = "account_number")
    private String accountNumber;
    @Column(nullable = false , name = "balance")
    private BigDecimal balance;
    @Column(nullable = false , name = "transaction_amount")
    private BigDecimal transactionAmount;
    private String content;
    @Column(nullable = false , name = "transaction_time")
    private LocalDateTime transactionDate;

//    public String getBalanceAsMoney(){
//        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
//        String formattedAmount = decimalFormat.format(balance);
//        return formattedAmount + " VND";
//    }
//    public String getTransactionAmountAsMoney(){
//        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
//        String formattedAmount = decimalFormat.format(balance);
//        return formattedAmount + " VND";
//    }
}
