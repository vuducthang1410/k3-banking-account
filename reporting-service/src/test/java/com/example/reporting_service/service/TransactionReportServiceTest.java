package com.example.reporting_service.service;

import com.example.reporting_service.mockdata.MockGrpcTransactionResponse;
import com.example.reporting_service.model.dto.TransactionsFilterRequest;
import com.example.reporting_service.model.entity.Report;
import com.system.common_library.dto.report.TransactionReportDTO;
import com.system.common_library.enums.ReportType;
import com.system.common_library.enums.State;
import com.system.common_library.service.TransactionDubboService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionReportServiceTest {
    @InjectMocks
    private AccountReportService accountReportService;

    @InjectMocks
    private LoanReportService loanReportService;

    @InjectMocks
    private TransactionReportService transactionReportService;

    @Mock
    private TransactionDubboService transactionDubboService;

    @Mock
    private ReportService reportService;

    @Mock
    private FilebaseStorageService filebaseStorageService;

    @Test
    void testCreateTransactionsReportByFilter() throws Exception {
        TransactionsFilterRequest mockRequest = new TransactionsFilterRequest();
        mockRequest.setCustomerId("CUST001");
        mockRequest.setStartDate(LocalDateTime.parse("2024-01-01T00:00:00"));
        mockRequest.setEndDate(LocalDateTime.parse("2024-12-31T23:59:59"));
        mockRequest.setMinAmount(1000.0);
        mockRequest.setMaxAmount(5000.0);
        mockRequest.setSenderAccountNumber("123456789");
        mockRequest.setRecipientAccountNumber("987654321");

        List<TransactionReportDTO> mockTransactions = MockGrpcTransactionResponse.generateMockTransactions(10);
        Report mockReport = new Report();
        mockReport.setId(1L);

        when(reportService.createReport(ReportType.TRANSACTION,mockRequest.getCustomerId())).thenReturn(mockReport);
        when(transactionDubboService.getTransactionByFilter(any())).thenReturn(mockTransactions);
        when(filebaseStorageService.uploadFile(any(), any())).thenReturn("https://filebase.com/mock_transaction_report.pdf");

        String fileUrl = transactionReportService.createTransactionsReportByFilter(mockRequest);

        assertNotNull(fileUrl);
        assertTrue(fileUrl.startsWith("https://filebase.com/"));

        verify(reportService).updateReportStatus(any(), eq(State.COMPLETED), any());
    }
}
