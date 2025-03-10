package com.example.reporting_service.service;

import com.example.reporting_service.mockdata.MockGrpcAccountResponse;
import com.example.reporting_service.mockdata.MockGrpcCustomerResponse;
import com.example.reporting_service.mockdata.MockGrpcTransactionResponse;
import com.example.reporting_service.model.dto.AccountsFilterRequest;
import com.example.reporting_service.model.dto.PersonalAccountRequest;
import com.example.reporting_service.model.entity.Report;
import com.system.common_library.dto.report.AccountReportResponse;
import com.system.common_library.dto.report.TransactionReportDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.AccountType;
import com.system.common_library.enums.State;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.CustomerDubboService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountReportServiceTest {

    @InjectMocks  // Tự động tiêm mock vào accountReportService
    private AccountReportService accountReportService;

    @Mock
    private AccountDubboService accountDubboService;

    @Mock
    private CustomerDubboService customerDubboService;

    @Mock
    private TransactionDubboService transactionDubboService;

    @Mock
    private ReportStatusService reportStatusService;

    @Test
    void testCreateAccountReport() throws Exception {
        // Giả lập dữ liệu đầu vào
        PersonalAccountRequest mockRequest = new PersonalAccountRequest();
        mockRequest.setAccount("12345");
        mockRequest.setAccountType(AccountType.SAVINGS);
        mockRequest.setCustomerId("CUST001");
        LocalDateTime startDate = LocalDateTime.parse("2025-01-01T00:00:00");
        LocalDateTime endDate = LocalDateTime.parse("2025-03-01T00:00:00");
        mockRequest.setStartTransactionDate(startDate);
        mockRequest.setEndTransactionDate(endDate);

        // Giả lập các service trả về dữ liệu giả
        AccountReportResponse mockAccountDetail = MockGrpcAccountResponse.getSingleMockAccountReport();
        CustomerDetailDTO mockCustomerDetail = MockGrpcCustomerResponse.getSingleMockCustomer();
        List<TransactionReportDTO> mockTransactions = MockGrpcTransactionResponse.generateMockTransactions(5);

        // Giả lập các phương thức trong các dịch vụ
        when(accountDubboService.getReportAccount(any(), any())).thenReturn(mockAccountDetail);
        when(customerDubboService.getCustomerByCustomerId(any())).thenReturn(mockCustomerDetail);
        when(transactionDubboService.getTransactionByFilter(any())).thenReturn(mockTransactions);
        when(reportStatusService.createReport(any())).thenReturn(new Report()); // Trả về đối tượng báo cáo mock

        // Gọi phương thức cần test
        byte[] resultPdf = accountReportService.createAccountReport(mockRequest);

        // Kiểm tra kết quả trả về (file PDF không null và có nội dung)
        assertNotNull(resultPdf);
        assertTrue(resultPdf.length > 0);

        // Kiểm tra xem các phương thức đã được gọi đúng
        verify(accountDubboService).getReportAccount(any(), any());
        verify(customerDubboService).getCustomerByCustomerId(any());
        verify(transactionDubboService).getTransactionByFilter(any());
        verify(reportStatusService).createReport(any());
        verify(reportStatusService).updateReportStatus(any(), eq(State.COMPLETED));
    }

    @Test
    void testCreateAccountsReportByFilter() throws Exception {
        // Giả lập dữ liệu đầu vào
        AccountsFilterRequest mockRequest = new AccountsFilterRequest();
        // Set dữ liệu cho mockRequest nếu cần

        List<AccountReportResponse> mockAccounts = MockGrpcAccountResponse.generateMockAccountReports(20);
        List<CustomerDetailDTO> mockCustomers = MockGrpcCustomerResponse.generateMockCustomers(20);

        // Giả lập các service trả về dữ liệu giả
        when(accountDubboService.getReportAccounts(any())).thenReturn(mockAccounts);
        when(customerDubboService.getReportCustomersByList(any())).thenReturn(mockCustomers);
        when(reportStatusService.createReport(any())).thenReturn(new Report()); // Trả về đối tượng báo cáo mock

        // Gọi phương thức cần test
        byte[] resultPdf = accountReportService.createAccountsReportByFilter(mockRequest);

        // Kiểm tra kết quả trả về (file PDF không null và có nội dung)
        assertNotNull(resultPdf);
        assertTrue(resultPdf.length > 0);

        // Kiểm tra xem các phương thức đã được gọi đúng
        verify(accountDubboService).getReportAccounts(any());
        verify(customerDubboService).getReportCustomersByList(any());
        verify(reportStatusService).createReport(any());
        verify(reportStatusService).updateReportStatus(any(), eq(State.COMPLETED));
    }


}
