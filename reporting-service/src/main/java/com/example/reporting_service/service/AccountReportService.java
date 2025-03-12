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
import com.system.common_library.enums.ObjectStatus;
import com.system.common_library.enums.ReportType;
import com.system.common_library.enums.State;
import com.system.common_library.enums.TransactionType;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.CustomerDubboService;
import com.system.common_library.service.TransactionDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@DubboService
public class AccountReportService {
    @Autowired
    private ReportService reportService;

    @Autowired
    private FilebaseStorageService filebaseStorageService;

    @DubboReference
    private AccountDubboService accountDubboService;

    @DubboReference
    private CustomerDubboService customerDubboService;

    @DubboReference
    private TransactionDubboService transactionDubboService;

    public String createMockAccountReport(PersonalAccountRequest request) throws Exception {
        if(request == null){
            System.out.println("ko nhận được request");
        }
        // 1. Tạo report với trạng thái PENDING
        Report report = reportService.createReport(ReportType.ACCOUNT, request.getCustomerId());

        try {
            // 2. Tạo dữ liệu mock
            AccountReportResponse accountDetail = AccountReportResponse.builder()
                    .customerId(request.getCustomerId())
                    .accountNumber(request.getAccount())
                    .accountType(request.getAccountType())
                    .status(ObjectStatus.ACTIVE)
                    .bankBranch("Hà Nội")
                    .balance(1000000.0)
                    .openedAt(LocalDateTime.now().minusYears(2))
                    .creditLimit("5000000")
                    .debtBalance("100000")
                    .rate(5.5)
                    .billingCycle(30)
                    .build();

            CustomerDetailDTO customerDetail = CustomerDetailDTO.builder()
                    .customerId(request.getCustomerId())
                    .cifCode("CIF123456")
                    .phone("0987654321")
                    .address("123 Đường ABC, Hà Nội")
                    .dob(LocalDate.of(1990, 1, 1))
                    .mail("nguyenvana@example.com")
                    .fullName("Nguyen Van A")
                    .firstName("Van A")
                    .lastName("Nguyen")
                    .identityCard("123456789")
                    .gender("Male")
                    .isActive(true)
                    .status(ObjectStatus.ACTIVE)
                    .customerNumber("CUS123456")
                    .build();

            List<TransactionReportDTO> transactions = List.of(
                    TransactionReportDTO.builder()
                            .transactionId("TXN123")
                            .account(request.getAccount())
                            .customerId(request.getCustomerId())
                            .senderAccountNumber("1111111111")
                            .recipientAccountNumber("2222222222")
                            .transactionDate(LocalDateTime.now().minusDays(1))
                            .amount(50000.0)
                            .transactionType(TransactionType.INTERNAL)
                            .status("SUCCESS")
                            .fee(5000.0)
                            .description("Mua hàng online")
                            .build(),
                    TransactionReportDTO.builder()
                            .transactionId("TXN124")
                            .account(request.getAccount())
                            .customerId(request.getCustomerId())
                            .senderAccountNumber("3333333333")
                            .recipientAccountNumber("4444444444")
                            .transactionDate(LocalDateTime.now().minusDays(2))
                            .amount(100000.0)
                            .transactionType(TransactionType.EXTERNAL)
                            .status("SUCCESS")
                            .fee(0.0)
                            .description("Nhận tiền lương")
                            .build()
            );

            // 3. Tạo file PDF
            byte[] pdfFile = generateAccountReportPdf(accountDetail, customerDetail, transactions);

            // 4. Upload file PDF lên Filebase (mock URL)
            String fileName = PdfLayout.generateReportFileName(ReportType.ACCOUNT);
            String fileUrl = filebaseStorageService.uploadFile(pdfFile, fileName);
            System.out.println(fileUrl);

            // 5. Cập nhật trạng thái & lưu đường dẫn file vào DB
            reportService.updateReportStatus(report.getId(), State.COMPLETED, fileUrl);

            // 6. Trả về đường dẫn file
            return fileUrl;
        } catch (Exception e) {
            // Nếu có lỗi, cập nhật trạng thái FAILED
            reportService.updateReportStatus(report.getId(), State.FAILED, null);
            throw new Exception("error.report.generation_failed", e);
        }
    }


    // get account,transaction,customer by gRPC to create pdf report
    public String createAccountReport(PersonalAccountRequest request) throws Exception {
        // 1. Tạo report với trạng thái PENDING
        Report report = reportService.createReport(ReportType.ACCOUNT, request.getCustomerId());

        try {
            // 2. Gọi Dubbo service để lấy dữ liệu
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

            // 3. Tạo file PDF
            byte[] pdfFile = generateAccountReportPdf(accountDetail, customerDetail, transactions);

            // 4. Upload file PDF lên Filebase
            String fileName = PdfLayout.generateReportFileName(ReportType.ACCOUNT);
            String fileUrl = filebaseStorageService.uploadFile(pdfFile, fileName);

            // 5. Cập nhật trạng thái & lưu đường dẫn file vào DB
            reportService.updateReportStatus(report.getId(), State.COMPLETED, fileUrl);

            // 6. Trả về đường dẫn file
            return fileUrl;
        } catch (Exception e) {
            // Nếu có lỗi, cập nhật trạng thái FAILED
            reportService.updateReportStatus(report.getId(), State.FAILED, null);
            throw new Exception("error.report.generation_failed", e);
        }
    }

    public String createAccountsReportByFilter(AccountsFilterRequest request) throws Exception {
        // 1. Tạo report với trạng thái PENDING
        Report report = reportService.createReport(ReportType.ACCOUNT_LIST, request.getCustomerId());

        try {
            // 2. Chuẩn bị request cho gRPC
            AccountReportRequest gRpcRequest = AccountReportRequest.builder()
                    .accountType(request.getAccountType())
                    .bankBranch(request.getBankBranch())
                    .startBalance(request.getStartBalance())
                    .endBalance(request.getEndBalance())
                    .startAt(request.getStartAt())
                    .endAt(request.getEndAt())
                    .status(request.getStatus())
                    .build();

            // 3. Lấy danh sách tài khoản
            List<AccountReportResponse> accounts = Optional.ofNullable(
                    accountDubboService.getReportAccounts(gRpcRequest)
            ).orElseThrow(() -> new Exception("error.dubbo.service_unavailable"));

            // 4. Lấy danh sách khách hàng từ danh sách customerId của tài khoản
            List<String> customerIds = accounts.stream()
                    .map(AccountReportResponse::getCustomerId)
                    .distinct()
                    .collect(Collectors.toList());

            List<CustomerDetailDTO> customers = Optional.ofNullable(
                    customerDubboService.getReportCustomersByList(customerIds)
            ).orElseThrow(() -> new Exception("error.customer.service_unavailable"));

            // 5. Trộn dữ liệu tài khoản & khách hàng
            List<AccountTableReport> mergedList = mergeAccountAndCustomerData(accounts, customers);

            // 6. Tạo file PDF
            byte[] pdfFile = generateAccountsReportPdf(mergedList);

            // 7. Upload file PDF lên Filebase
            String fileName = PdfLayout.generateReportFileName(ReportType.ACCOUNT_LIST);
            String fileUrl = filebaseStorageService.uploadFile(pdfFile, fileName);

            // 8. Cập nhật trạng thái & lưu đường dẫn file vào DB
            reportService.updateReportStatus(report.getId(), State.COMPLETED, fileUrl);

            // 9. Trả về đường dẫn file
            return fileUrl;
        } catch (Exception e) {
            // Nếu có lỗi, cập nhật trạng thái FAILED
            reportService.updateReportStatus(report.getId(), State.FAILED, null);
            throw new Exception("error.report.generation_failed", e);
        }
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
