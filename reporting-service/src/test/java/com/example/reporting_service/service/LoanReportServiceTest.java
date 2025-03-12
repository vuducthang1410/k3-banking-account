package com.example.reporting_service.service;

import com.example.reporting_service.mockdata.MockGrpcAccountResponse;
import com.example.reporting_service.mockdata.MockGrpcCustomerResponse;
import com.example.reporting_service.mockdata.MockGrpcLoanResponse;
import com.example.reporting_service.mockdata.MockGrpcTransactionResponse;
import com.example.reporting_service.model.dto.AccountsFilterRequest;
import com.example.reporting_service.model.dto.PersonalAccountRequest;
import com.example.reporting_service.model.dto.PersonalLoanRequest;
import com.example.reporting_service.model.entity.Report;
import com.system.common_library.dto.report.AccountReportResponse;
import com.system.common_library.dto.report.LoanReportResponse;
import com.system.common_library.dto.report.TransactionReportDTO;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.AccountType;
import com.system.common_library.enums.State;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.CustomerDubboService;
import com.system.common_library.service.LoanDubboService;
import com.system.common_library.service.TransactionDubboService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoanReportServiceTest {

    @InjectMocks
    private AccountReportService accountReportService;

    @InjectMocks
    private LoanReportService loanReportService;

    @Mock
    private AccountDubboService accountDubboService;

    @Mock
    private CustomerDubboService customerDubboService;

    @Mock
    private TransactionDubboService transactionDubboService;

    @Mock
    private LoanDubboService loanDubboService;

    @Mock
    private ReportService reportService;

    @Mock
    private FilebaseStorageService filebaseStorageService;

    @Test
    void testCreateAccountReport() throws Exception {
        PersonalAccountRequest mockRequest = new PersonalAccountRequest();
        mockRequest.setAccount("12345");
        mockRequest.setAccountType(AccountType.SAVINGS);
        mockRequest.setCustomerId("CUST001");

        AccountReportResponse mockAccountDetail = MockGrpcAccountResponse.getSingleMockAccountReport();
        CustomerDetailDTO mockCustomerDetail = MockGrpcCustomerResponse.getSingleMockCustomer();
        List<TransactionReportDTO> mockTransactions = MockGrpcTransactionResponse.generateMockTransactions(5);

        when(accountDubboService.getReportAccount(any(), any())).thenReturn(mockAccountDetail);
        when(customerDubboService.getCustomerByCustomerId(any())).thenReturn(mockCustomerDetail);
        when(transactionDubboService.getTransactionByFilter(any())).thenReturn(mockTransactions);
        when(reportService.createReport(any(), any())).thenReturn(new Report());
        when(filebaseStorageService.uploadFile(any(), any())).thenReturn("https://filebase.com/mock_report.pdf");

        String fileUrl = accountReportService.createAccountReport(mockRequest);

        assertNotNull(fileUrl);
        assertTrue(fileUrl.startsWith("https://filebase.com/"));

        verify(reportService).updateReportStatus(any(), eq(State.COMPLETED), any());
    }

    @Test
    void testCreateAccountsReportByFilter() throws Exception {
        AccountsFilterRequest mockRequest = new AccountsFilterRequest();
        List<AccountReportResponse> mockAccounts = MockGrpcAccountResponse.generateMockAccountReports(20);
        List<CustomerDetailDTO> mockCustomers = MockGrpcCustomerResponse.generateMockCustomers(20);

        when(accountDubboService.getReportAccounts(any())).thenReturn(mockAccounts);
        when(customerDubboService.getReportCustomersByList(any())).thenReturn(mockCustomers);
        when(reportService.createReport(any(), any())).thenReturn(new Report());
        when(filebaseStorageService.uploadFile(any(), any())).thenReturn("https://filebase.com/mock_list_report.pdf");

        String fileUrl = accountReportService.createAccountsReportByFilter(mockRequest);

        assertNotNull(fileUrl);
        assertTrue(fileUrl.startsWith("https://filebase.com/"));

        verify(reportService).updateReportStatus(any(), eq(State.COMPLETED), any());
    }

    @Test
    void testCreateLoanReport() throws Exception {
        PersonalLoanRequest mockRequest = new PersonalLoanRequest();
        mockRequest.setLoanId("LOAN123");
        mockRequest.setAccount("12345");
        mockRequest.setCustomerId("CUST001");

        LoanReportResponse mockLoan = MockGrpcLoanResponse.getSingleMockLoan();
        AccountReportResponse mockAccountDetail = MockGrpcAccountResponse.getSingleMockAccountReport();
        CustomerDetailDTO mockCustomerDetail = MockGrpcCustomerResponse.getSingleMockCustomer();

        when(loanDubboService.getLoanReport(any())).thenReturn(mockLoan);
        when(accountDubboService.getReportAccount(any(), any())).thenReturn(mockAccountDetail);
        when(customerDubboService.getCustomerByCustomerId(any())).thenReturn(mockCustomerDetail);
        when(reportService.createReport(any(), any())).thenReturn(new Report());
        when(filebaseStorageService.uploadFile(any(), any())).thenReturn("https://filebase.com/mock_loan_report.pdf");

        String fileUrl = loanReportService.createLoanReport(mockRequest);

        assertNotNull(fileUrl);
        assertTrue(fileUrl.startsWith("https://filebase.com/"));

        verify(reportService).updateReportStatus(any(), eq(State.COMPLETED), any());
    }
}


