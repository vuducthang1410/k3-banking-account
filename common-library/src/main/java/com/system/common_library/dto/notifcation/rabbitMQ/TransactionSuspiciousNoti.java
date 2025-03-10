package com.system.common_library.dto.notifcation.rabbitMQ;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TransactionSuspiciousNoti implements Serializable {
    String accountNumber;
    LocalDateTime transactionTime;
    BigDecimal transactionAmount;
    String customerCIF;

//    public String getTransactionTime(){
//        if (transactionTime == null) {
//            return "No transaction Time set";
//        }
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
//        return transactionTime.format(formatter);
//    }
//    public String getTransactionAmountAsMoney(){
//        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
//        String formattedAmount = decimalFormat.format(transactionAmount);
//        return formattedAmount + " VND";
//    }
}
