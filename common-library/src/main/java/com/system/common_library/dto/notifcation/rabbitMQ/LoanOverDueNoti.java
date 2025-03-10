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
public class LoanOverDueNoti implements Serializable {
    String contractNumber;
    //so tien qua han
    BigDecimal amountDue;
    //Original due date
    LocalDate dueDate;
    //Days overdue
    int overdueDays;
    //Penalty fee (if applicable)
    BigDecimal penaltyFee;
    String customerCIF;
}
