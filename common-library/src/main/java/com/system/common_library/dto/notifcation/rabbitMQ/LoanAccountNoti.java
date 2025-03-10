package com.system.common_library.dto.notifcation.rabbitMQ;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanAccountNoti implements Serializable {
    String accountNumber;
    BigDecimal loanDueAmount; //String
    LocalDate openDate;
    LocalDate loanDueDate;
    String customerCIF;
}
