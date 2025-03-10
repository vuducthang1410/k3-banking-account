package com.example.reporting_service.service;


import com.example.reporting_service.model.Enum.ReportTitle;
import com.example.reporting_service.model.dto.AccountTableReport;
import com.example.reporting_service.model.dto.AccountsFilterRequest;
import com.example.reporting_service.model.dto.PersonalAccountRequest;
import com.example.reporting_service.model.entity.Report;
import com.example.reporting_service.util.PdfLayout;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.system.common_library.dto.report.AccountReportRequest;
import com.system.common_library.dto.report.AccountReportResponse;
import com.system.common_library.dto.report.TransactionReportDTO;
import com.system.common_library.dto.report.TransactionReportRequest;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.ReportType;
import com.system.common_library.enums.State;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.CustomerDubboService;
import com.system.common_library.service.TransactionDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@DubboService
public class AccountReportService {
    @Autowired
    private ReportStatusService reportStatusService;

    @DubboReference
    private AccountDubboService accountDubboService;

    @DubboReference
    private CustomerDubboService customerDubboService;

    @DubboReference
    private TransactionDubboService transactionDubboService;

    // get account,transaction,customer by gRPC to create pdf report
    public byte[] createAccountReport(PersonalAccountRequest request) throws Exception {
        Report report = reportStatusService.createReport(ReportType.ACCOUNT);
        AccountReportResponse accountDetail = Optional.ofNullable(
                accountDubboService.getReportAccount(request.getAccount(), request.getAccountType())
        ).orElseThrow(() -> new Exception("error.dubbo.service_unavailable"));
        CustomerDetailDTO customerDetail = Optional.ofNullable(
                customerDubboService.getCustomerByCustomerId(request.getCustomerId())
        ).orElseThrow(() -> new Exception("error.dubbo.service_unavailable"));
        TransactionReportRequest transactionRequest = TransactionReportRequest.builder()
                .account(request.getAccount())
                .startDate(request.getStartTransactionDate())
                .endDate(request.getEndTransactionDate())
                .build();
        List<TransactionReportDTO> transactions = Optional.ofNullable(
                transactionDubboService.getTransactionByFilter(transactionRequest)
        ).orElseThrow(() -> new Exception("error.dubbo.service_unavailable"));
        byte[] pdfFile = generateAccountReportPdf(accountDetail, customerDetail, transactions);
        reportStatusService.updateReportStatus(report.getId(), State.COMPLETED);
        return pdfFile;
    }

    // get account list and customer list by gRPC to create account list report
    public byte[] createAccountsReportByFilter(AccountsFilterRequest request) throws Exception {
        AccountReportRequest gRpcRequest = AccountReportRequest.builder()
                .accountType(request.getAccountType())
                .bankBranch(request.getBankBranch())
                .startBalance(request.getStartBalance())
                .endBalance(request.getEndBalance())
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .status(request.getStatus())
                .build();
        Report report = reportStatusService.createReport(ReportType.ACCOUNT);
        List<AccountReportResponse> accounts = Optional.ofNullable(
                accountDubboService.getReportAccounts(gRpcRequest)
        ).orElseThrow(() -> new Exception("error.dubbo.service_unavailable"));
        List<String> customerIds = accounts.stream()
                .map(AccountReportResponse::getCustomerId)
                .distinct()
                .collect(Collectors.toList());
        List<CustomerDetailDTO> customers = Optional.ofNullable(
                customerDubboService.getReportCustomersByList(customerIds)
        ).orElseThrow(() -> new Exception("error.customer.service_unavailable"));
        List<AccountTableReport > mergedList = mergeAccountAndCustomerData(accounts, customers);
        byte[] pdfFile = generateAccountsReportPdf(mergedList);
        reportStatusService.updateReportStatus(report.getId(), State.COMPLETED);
        return pdfFile;
    }

    //Merges account data with corresponding customer information
    private List<AccountTableReport> mergeAccountAndCustomerData(List<AccountReportResponse> accounts,
                                                                 List<CustomerDetailDTO> customers) {
        Map<String, CustomerDetailDTO> customerMap = customers.stream()
                .collect(Collectors.toMap(CustomerDetailDTO::getCustomerId, customer -> customer));

        return accounts.stream()
                .map(account -> {
                    CustomerDetailDTO customer = customerMap.get(account.getCustomerId());
                    if (customer == null) {
                        return null; // Bỏ qua nếu không có thông tin khách hàng
                    }
                    return AccountTableReport.builder()
                            .customerId(account.getCustomerId())
                            .accountNumber(account.getAccountNumber())
                            .accountType(account.getAccountType())
                            .status(account.getStatus())
                            .bankBranch(account.getBankBranch())
                            .balance(account.getBalance())
                            .openedAt(account.getOpenedAt())
                            .creditLimit(account.getCreditLimit())
                            .debtBalance(account.getDebtBalance())
                            .rate(account.getRate())
                            .billingCycle(account.getBillingCycle())
                            .fullName(customer.getFullName())
                            .build();
                })
                .filter(Objects::nonNull) // Loại bỏ phần tử null nếu không có thông tin customer
                .toList(); // Java 16+ có thể dùng toList() thay cho collect(Collectors.toList())
    }

    private byte[] generateAccountReportPdf(AccountReportResponse accountDetail,
                                                  CustomerDetailDTO customerDetail,
                                                  List<TransactionReportDTO> transactions) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            PdfLayout.createHeader(document);
            PdfLayout.addReportTitle(document, ReportTitle.ACCOUNT_REPORT);
            PdfLayout.populateFromSingleDto(document, accountDetail);
            PdfLayout.populateFromSingleDto(document, customerDetail);
            PdfLayout.generateTableFromDTOList(document, ReportTitle.TRANSACTION_TABLE, transactions);
            PdfLayout.createFooter(document);
            document.close();
            return outputStream.toByteArray();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] generateAccountsReportPdf(List<AccountTableReport> mergedList) throws IOException, IllegalAccessException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            pdfDocument.setDefaultPageSize(PageSize.A4.rotate());
            Document document = new Document(pdfDocument);
            PdfLayout.createHeader(document);
            PdfLayout.addReportTitle(document, ReportTitle.ACCOUNT_REPORT);
            PdfLayout.generateTableFromDTOList(document, null, mergedList);
            PdfLayout.createFooter(document);
            document.close();
            return outputStream.toByteArray();
        }
    }
}
