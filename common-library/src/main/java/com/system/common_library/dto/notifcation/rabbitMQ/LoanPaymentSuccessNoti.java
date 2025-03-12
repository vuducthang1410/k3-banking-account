package com.system.common_library.dto.notifcation.rabbitMQ;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class LoanPaymentSuccessNoti implements Serializable {
    private String customerCIF;
    BigDecimal paymentAmount;
    String paymentType;
    LocalDate paymentDate;
    String loanContractNumber;
}
