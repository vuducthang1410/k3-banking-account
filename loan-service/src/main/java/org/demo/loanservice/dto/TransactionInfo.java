package org.demo.loanservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.demo.loanservice.dto.enumDto.PaymentType;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionInfo {
    private String transactionId;
    private PaymentType paymentType;
}
