package com.system.common_library.dto.notifcation.rabbitMQ;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoanFinancialReviewFailNoti implements Serializable {
    LocalDate expiryDate;
    String customerCIF;
}
