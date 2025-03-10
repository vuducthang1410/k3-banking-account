package com.system.common_library.dto.notifcation;

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
public class BalanceFluctuationNotificationDTO implements Serializable {
    @NotNull
    String customerCIF;
    @NotNull
    String accountNumber;
    @NotNull
    BigDecimal transactionAmount;
    @NotNull
    BigDecimal balance;
    @NotNull
    String transactionContent;
    @NotNull
    LocalDateTime transactionTime;
//    public String getBalanceAsMoney(){
//        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
//        String formattedAmount = decimalFormat.format(balance);
//        return formattedAmount + " VND";
//    }
//    public String getTransactionAmountAsMoney(){
//        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
//        String formattedAmount = decimalFormat.format(transactionAmount);
//        return formattedAmount + " VND";
//    }
}
