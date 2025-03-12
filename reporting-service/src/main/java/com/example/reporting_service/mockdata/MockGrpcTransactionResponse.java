package com.example.reporting_service.mockdata;

import com.system.common_library.dto.report.TransactionReportDTO;
import com.system.common_library.enums.TransactionType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MockGrpcTransactionResponse {
    private static final String[] STATUS_LIST = {"SUCCESS", "PENDING", "FAILED"};
    private static final TransactionType[] TRANSACTION_TYPES = TransactionType.values();
    private static final Random RANDOM = new Random();

    public static List<TransactionReportDTO> generateMockTransactions(int count) {
        List<TransactionReportDTO> transactions = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            transactions.add(TransactionReportDTO.builder()
                    .transactionId("TXN" + (100000 + i))
                    .account("ACC" + (1000 + i))
                    .customerId("CUST" + String.format("%03d", i))
                    .senderAccountNumber("12345" + (10000 + i))
                    .recipientAccountNumber("54321" + (20000 + i))
                    .transactionDate(LocalDateTime.now().minusDays(i % 30))
                    .amount(100000.0 * (i % 10 + 1))
                    .transactionType(TRANSACTION_TYPES[i % TRANSACTION_TYPES.length])
                    .status(STATUS_LIST[i % STATUS_LIST.length])
                    .fee(1000.0 * (i % 5 + 1))
                    .description("Giao dịch tự động " + i)
                    .build());
        }
        return transactions;
    }
}
