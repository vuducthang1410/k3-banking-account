package com.example.reporting_service.model.dto;

import com.system.common_library.enums.State;
import com.system.common_library.enums.TransactionType;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionsFilterRequest {@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
private LocalDateTime startDate;

    private String customerId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    private Double minAmount;
    private Double maxAmount;
    private TransactionType transactionType;
    private State transactionStatus;
    private String senderAccountNumber;
    private String recipientAccountNumber;

}
