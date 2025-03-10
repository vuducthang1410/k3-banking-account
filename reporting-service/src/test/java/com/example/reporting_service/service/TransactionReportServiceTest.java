package com.example.reporting_service.service;

import com.example.reporting_service.mockdata.MockGrpcTransactionResponse;
import com.example.reporting_service.model.dto.TransactionsFilterRequest;
import com.example.reporting_service.model.entity.Report;
import com.system.common_library.dto.report.TransactionReportDTO;
import com.system.common_library.enums.ReportType;
import com.system.common_library.enums.State;
import com.system.common_library.enums.TransactionType;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionReportServiceTest {
    @InjectMocks
    private TransactionReportService transactionReportService;

    @Mock
    private ReportStatusService reportStatusService;

    @Mock
    private TransactionDubboService transactionDubboService;

    @Test
    void testCreateTransactionsReportByFilter() throws Exception {
        // Mock dữ liệu đầu vào
        TransactionsFilterRequest mockRequest = new TransactionsFilterRequest();
        mockRequest.setStartDate(LocalDateTime.parse("2024-01-01T00:00:00"));
        mockRequest.setEndDate(LocalDateTime.parse("2024-12-31T23:59:59"));
        mockRequest.setMinAmount(1000.0);
        mockRequest.setMaxAmount(5000.0);
        mockRequest.setTransactionType(TransactionType.EXTERNAL);
        mockRequest.setTransactionStatus(State.COMPLETED);
        mockRequest.setSenderAccountNumber("123456789");
        mockRequest.setRecipientAccountNumber("987654321");

        // Mock dữ liệu transaction
        List<TransactionReportDTO> mockTransactions = MockGrpcTransactionResponse.generateMockTransactions(10);

        // Mock hành vi của reportStatusService và transactionDubboService
        Report mockReport = new Report();
        mockReport.setId(1L);
        when(reportStatusService.createReport(ReportType.TRANSACTION)).thenReturn(mockReport);
        when(transactionDubboService.getTransactionByFilter(any())).thenReturn(mockTransactions);
        doNothing().when(reportStatusService).updateReportStatus(anyLong(), any());

        // Gọi phương thức cần test
        byte[] pdfFile = transactionReportService.createTransactionsReportByFilter(mockRequest);

        // Kiểm tra kết quả
        assertNotNull(pdfFile);
        assertTrue(pdfFile.length > 0);

        // Kiểm tra phương thức được gọi đúng
        verify(reportStatusService).createReport(ReportType.TRANSACTION);
        verify(transactionDubboService).getTransactionByFilter(any());
        verify(reportStatusService).updateReportStatus(mockReport.getId(), State.COMPLETED);
    }
}
