package com.system.common_library.dto.response.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionNotificationDTO implements Serializable {

    @NotNull
    private String customerCIF;
    @NotNull
    private BigDecimal balance;
    // Transaction type
    private String transactionType;
    // Transaction code
    private String transactionCode;
    // Debit account
    private String debitAccount;
    // Account owner / Customer name
    private String accountOwner;
    // Beneficiary account
    private String beneficiaryAccount;
    // Beneficiary name
    private String beneficiaryName;
    // Beneficiary bank
    private String beneficiaryBanK;
    // Transaction date
    private LocalDateTime transactionDate;
    // Debit amount
    private BigDecimal debitAmount;
    // Details of transaction / note
    private String detailsOfTransaction;
    // Fee
    private BigDecimal fee;
    // Charge type
    private String chargeType;

    private boolean isSuccess;

//    public String getBalanceAsMoney(){
//        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
//        String formattedAmount = decimalFormat.format(balance);
//        return formattedAmount + " VND";
//    }
//    public String getDebitAmountAsMoney(){
//        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
//        String formattedAmount = decimalFormat.format(debitAmount);
//        return formattedAmount + " VND";
//    }
//    public String getFeeAsMoney(){
//        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
//        String formattedAmount = decimalFormat.format(fee);
//        return formattedAmount + " VND";
//    }
//    public String getTransactionDateAsDate(){
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        return transactionDate.format(formatter);
//    }
//    public String getTransactionDateAsDateTime(){
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
//        return transactionDate.format(formatter);
//    }
}
