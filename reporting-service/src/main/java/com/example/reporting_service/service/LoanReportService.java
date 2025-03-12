package com.example.reporting_service.service;

import com.example.reporting_service.model.Enum.ReportTitle;
import com.example.reporting_service.model.dto.LoansFilterRequest;
import com.example.reporting_service.model.dto.PersonalLoanRequest;
import com.example.reporting_service.model.entity.Report;
import com.example.reporting_service.util.PdfLayout;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.system.common_library.dto.report.AccountReportResponse;
import com.system.common_library.dto.report.LoanReportRequest;
import com.system.common_library.dto.report.LoanReportResponse;
import com.system.common_library.dto.user.CustomerDetailDTO;
import com.system.common_library.enums.ReportType;
import com.system.common_library.enums.State;
import com.system.common_library.exception.DubboException;
import com.system.common_library.service.AccountDubboService;
import com.system.common_library.service.CustomerDubboService;
import com.system.common_library.service.LoanDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Service
public class LoanReportService {
    @Autowired
    private ReportService reportService;

    @Autowired
    private FilebaseStorageService filebaseStorageService;

    @DubboReference
    private LoanDubboService loanDubboService;

    @DubboReference
    private CustomerDubboService customerDubboService;

    @DubboReference
    private AccountDubboService accountDubboService;

    public String createLoanReport(PersonalLoanRequest request) throws Exception {
        // 1. Tạo report với trạng thái PENDING
        Report report = reportService.createReport(ReportType.LOAN, request.getCustomerId());

        try {
            // 2. Lấy dữ liệu khoản vay
            LoanReportResponse loan = Optional.ofNullable(
                    loanDubboService.getLoanReport(request.getLoanId())
            ).orElseThrow(() -> new Exception("error.dubbo.service_unavailable"));

            // 3. Lấy dữ liệu tài khoản liên quan
            AccountReportResponse accountDetail = Optional.ofNullable(
                    accountDubboService.getReportAccount(request.getAccount(), request.getAccountType())
            ).orElseThrow(() -> new Exception("error.dubbo.service_unavailable"));

            // 4. Lấy dữ liệu khách hàng liên quan
            CustomerDetailDTO customerDetail = Optional.ofNullable(
                    customerDubboService.getCustomerByCustomerId(request.getCustomerId())
            ).orElseThrow(() -> new Exception("error.dubbo.service_unavailable"));

            // 5. Tạo file PDF
            byte[] pdfFile = generateLoanReportPdf(loan, accountDetail, customerDetail);

            // 6. Upload file PDF lên Filebase
            String fileName = PdfLayout.generateReportFileName(ReportType.LOAN);
            String fileUrl = filebaseStorageService.uploadFile(pdfFile, fileName);

            // 7. Cập nhật trạng thái & lưu đường dẫn file vào DB
            reportService.updateReportStatus(report.getId(), State.COMPLETED, fileUrl);

            // 8. Trả về đường dẫn file PDF
            return fileUrl;
        } catch (Exception e) {
            // Nếu có lỗi, cập nhật trạng thái FAILED
            reportService.updateReportStatus(report.getId(), State.FAILED, null);
            throw new Exception("error.report.generation_failed", e);
        }
    }


    public String createLoansReportByFilter(LoansFilterRequest request) throws Exception {
        // 1. Tạo report với trạng thái PENDING
        Report report = reportService.createReport(ReportType.LOAN_LIST, request.getCustomerId());

        try {
            // 2. Chuẩn bị request cho gRPC
            LoanReportRequest gRpcRequest = LoanReportRequest.builder()
                    .loanId(request.getLoanId())
                    .customerId(request.getCustomerId())
                    .minLoanAmount(request.getMinLoanAmount())
                    .maxLoanAmount(request.getMaxLoanAmount())
                    .loanType(request.getLoanType())
                    .startDate(request.getStartAccountDate())
                    .endDate(request.getEndAccountDate())
                    .loanStatus(request.getLoanStatus())
                    .build();

            // 3. Gọi gRPC lấy danh sách khoản vay
            List<LoanReportResponse> loans = Optional.ofNullable(
                            loanDubboService.getListLoanByField(gRpcRequest)
                    ).filter(list -> !list.isEmpty())
                    .orElseThrow(() -> new DubboException("error.dubbo.service_unavailable"));

            // 4. Tạo file PDF
            byte[] pdfFile = generateLoansReportPdf(loans);

            // 5. Upload file PDF lên Filebase
            String fileName = PdfLayout.generateReportFileName(ReportType.LOAN_LIST);
            String fileUrl = filebaseStorageService.uploadFile(pdfFile, fileName);

            // 6. Cập nhật trạng thái & lưu đường dẫn vào DB
            reportService.updateReportStatus(report.getId(), State.COMPLETED, fileUrl);

            // 7. Trả về đường dẫn file PDF
            return fileUrl;
        } catch (Exception e) {
            // Nếu có lỗi, cập nhật trạng thái FAILED
            reportService.updateReportStatus(report.getId(), State.FAILED, null);
            throw new Exception("error.report.generation_failed", e);
        }
    }


    private byte[] generateLoanReportPdf(LoanReportResponse loan,
                                               AccountReportResponse accountDetail,
                                               CustomerDetailDTO customerDetail) throws IOException, IllegalAccessException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            Document document = new Document(pdfDocument);
            PdfLayout.createHeader(document);
            PdfLayout.addReportTitle(document, ReportTitle.LOAN_REPORT);


            PdfLayout.populateFromSingleDto(document, loan);
            PdfLayout.populateFromSingleDto(document, accountDetail);
            PdfLayout.populateFromSingleDto(document, customerDetail);
            PdfLayout.generateTableFromDTOList(document,ReportTitle.LOAN_PAYMENT_SCHEDULE,loan.getPaymentScheduleList());
            PdfLayout.createFooter(document);
            document.close();
            return outputStream.toByteArray();
        }
    }

    private byte[] generateLoansReportPdf(List<LoanReportResponse> loans) throws IOException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            pdfDocument.setDefaultPageSize(PageSize.A4.rotate());
            Document document = new Document(pdfDocument);
            PdfLayout.createHeader(document);
            PdfLayout.addReportTitle(document, ReportTitle.LOAN_REPORT);
            PdfLayout.generateTableFromDTOList(document, null, loans);
            PdfLayout.createFooter(document);
            document.close();
            return outputStream.toByteArray();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

}
