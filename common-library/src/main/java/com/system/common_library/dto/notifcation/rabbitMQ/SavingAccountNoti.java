package com.system.common_library.dto.notifcation.rabbitMQ;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SavingAccountNoti implements Serializable {
    String accountNumber;
    BigDecimal depositAmount;
    String term;
    Double interestRate;
    LocalDate openDate;
    String customerCIF;
}
