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
    private ReportStatusService reportStatusService;

    @DubboReference
    private LoanDubboService loanDubboService;

    @DubboReference
    private CustomerDubboService customerDubboService;

    @DubboReference
    private AccountDubboService accountDubboService;

    public byte[] createLoanReport(PersonalLoanRequest request) throws Exception {
        Report report = reportStatusService.createReport(ReportType.LOAN);
        LoanReportResponse loan = Optional.ofNullable(
                loanDubboService.getLoanReport(request.getLoanId())
        ).orElseThrow(() -> new Exception("error.dubbo.service_unavailable"));
        AccountReportResponse accountDetail = Optional.ofNullable(
                accountDubboService.getReportAccount(request.getAccount(), request.getAccountType())
        ).orElseThrow(() -> new Exception("error.dubbo.service_unavailable"));
        CustomerDetailDTO customerDetail = Optional.ofNullable(
                customerDubboService.getCustomerByCustomerId(request.getCustomerId())
        ).orElseThrow(() -> new Exception("error.dubbo.service_unavailable"));
        byte[] pdfFile = generateLoanReportPdf(loan, accountDetail, customerDetail);
        reportStatusService.updateReportStatus(report.getId(), State.COMPLETED);
        return pdfFile;
    }

    public byte[] createLoansReportByFilter(LoansFilterRequest request) throws Exception {
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
        Report report = reportStatusService.createReport(ReportType.LOAN);
        List<LoanReportResponse> loans = Optional.ofNullable(
                        loanDubboService.getListLoanByField(gRpcRequest)
                ).filter(list -> !list.isEmpty())
                .orElseThrow(() -> new DubboException("error.dubbo.service_unavailable"));
        byte[] pdfFile = generateLoansReportPdf(loans);
        reportStatusService.updateReportStatus(report.getId(), State.COMPLETED);
        return pdfFile;
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
