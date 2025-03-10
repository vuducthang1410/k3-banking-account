package com.example.reporting_service.service;

import com.example.reporting_service.mockdata.MockGrpcAccountResponse;
import com.example.reporting_service.mockdata.MockGrpcCustomerResponse;
import com.example.reporting_service.mockdata.MockGrpcLoanResponse;
import com.example.reporting_service.model.dto.LoansFilterRequest;
import com.example.reporting_service.model.dto.PersonalLoanRequest;
import com.example.reporting_service.model.entity.Report;
import com.system.common_library.dto.report.AccountReportResponse;
import com.system.common_library.dto.report.LoanReportRequest;
import com.system.common_library.dto.report.LoanReportResponse;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.*;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.CustomerDubboService;
import com.system.common_library.service.LoanDubboService;
import com.system.common_library.service.TransactionDubboService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoanReportServiceTest {

    @InjectMocks
    private LoanReportService loanReportService;

    @Mock
    private LoanDubboService loanDubboService;

    @Mock
    private AccountDubboService accountDubboService;

    @Mock
    private CustomerDubboService customerDubboService;

    @Mock
    private TransactionDubboService transactionDubboService;

    @Mock
    private ReportStatusService reportStatusService;

    @Test
    void testCreateLoanReport() throws Exception {
        // Giả lập dữ liệu đầu vào
        PersonalLoanRequest mockRequest = new PersonalLoanRequest();
        mockRequest.setLoanId("LOAN123");
        mockRequest.setAccount("12345");
        mockRequest.setAccountType(AccountType.SAVINGS);
        mockRequest.setCustomerId("CUST001");

        // Giả lập dữ liệu từ các service
        LoanReportResponse mockLoan = MockGrpcLoanResponse.getSingleMockLoan();
        AccountReportResponse mockAccountDetail = MockGrpcAccountResponse.getSingleMockAccountReport();
        CustomerDetailDTO mockCustomerDetail = MockGrpcCustomerResponse.getSingleMockCustomer();

        Report mockReport = new Report();
        when(reportStatusService.createReport(any())).thenReturn(mockReport);
        when(loanDubboService.getLoanReport(any())).thenReturn(mockLoan);
        when(accountDubboService.getReportAccount(any(), any())).thenReturn(mockAccountDetail);
        when(customerDubboService.getCustomerByCustomerId(any())).thenReturn(mockCustomerDetail);

        // Gọi phương thức cần test
        byte[] resultPdf = loanReportService.createLoanReport(mockRequest);

        // Kiểm tra kết quả trả về
        assertNotNull(resultPdf);
        assertTrue(resultPdf.length > 0);

        // Kiểm tra xem các phương thức đã được gọi đúng
        verify(reportStatusService).createReport(any());
        verify(loanDubboService).getLoanReport(any());
        verify(accountDubboService).getReportAccount(any(), any());
        verify(customerDubboService).getCustomerByCustomerId(any());
        verify(reportStatusService).updateReportStatus(any(), eq(State.COMPLETED));
    }

    @Test
    void testCreateLoanReport_LoanServiceUnavailable() throws Exception {
        PersonalLoanRequest mockRequest = new PersonalLoanRequest();
        mockRequest.setLoanId("LOAN123");
        mockRequest.setAccount("12345");
        mockRequest.setAccountType(AccountType.SAVINGS);
        mockRequest.setCustomerId("CUST001");

        Report mockReport = new Report();
        when(reportStatusService.createReport(any())).thenReturn(mockReport);
        when(loanDubboService.getLoanReport(any())).thenReturn(null); // Giả lập lỗi

        Exception exception = assertThrows(Exception.class, () -> {
            loanReportService.createLoanReport(mockRequest);
        });

        assertEquals("error.dubbo.service_unavailable", exception.getMessage());

        verify(reportStatusService).createReport(any());
        verify(loanDubboService).getLoanReport(any());
        verify(reportStatusService, never()).updateReportStatus(any(), any());
    }


    @Test
    void testCreateLoansReportByFilter_Success() throws Exception {
        // Giả lập request đầu vào
        LoansFilterRequest mockRequest = new LoansFilterRequest();
        mockRequest.setLoanId("L12345");
        mockRequest.setCustomerId("C67890");
        mockRequest.setMinLoanAmount(10000.0);
        mockRequest.setMaxLoanAmount(50000.0);
        mockRequest.setLoanType(FormDeftRepaymentEnum.PRINCIPAL_AND_INTEREST_MONTHLY);
        mockRequest.setStartAccountDate(LocalDate.parse("2024-01-01"));
        mockRequest.setEndAccountDate(LocalDate.parse("2024-12-31"));

        mockRequest.setLoanStatus(LoanStatus.ACTIVE);

        // Giả lập danh sách khoản vay từ gRPC
        List<LoanReportResponse> mockLoans = MockGrpcLoanResponse.generateMockLoans(3);
        when(loanDubboService.getListLoanByField(any(LoanReportRequest.class)))
                .thenReturn(mockLoans);

        // Giả lập report được tạo
        Report mockReport = new Report();
        mockReport.setId(1L);
        when(reportStatusService.createReport(ReportType.LOAN))
                .thenReturn(mockReport);

        // Gọi phương thức cần kiểm thử
        byte[] resultPdf = loanReportService.createLoansReportByFilter(mockRequest);

        // Kiểm tra kết quả
        assertNotNull(resultPdf);
        assertTrue(resultPdf.length > 0);

        // Kiểm tra xem report đã được cập nhật trạng thái hay chưa
        verify(reportStatusService, times(1)).updateReportStatus(1L, State.COMPLETED);
    }

    @Test
    void testCreateLoansReportByFilter_ThrowsException() {
        // Giả lập request đầu vào
        LoansFilterRequest mockRequest = new LoansFilterRequest();
        mockRequest.setLoanId("L12345");

        // Giả lập gRPC trả về null
        when(loanDubboService.getListLoanByField(any(LoanReportRequest.class)))
                .thenReturn(null);

        // Kiểm tra ngoại lệ được ném ra
        Exception exception = assertThrows(Exception.class, () -> {
            loanReportService.createLoansReportByFilter(mockRequest);
        });

        assertEquals("error.dubbo.service_unavailable", exception.getMessage());
    }
}


