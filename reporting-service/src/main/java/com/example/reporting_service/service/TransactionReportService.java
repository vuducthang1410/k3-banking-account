package com.example.reporting_service.service;

import com.example.reporting_service.model.Enum.ReportTitle;
import com.example.reporting_service.model.dto.TransactionsFilterRequest;
import com.example.reporting_service.model.entity.Report;
import com.example.reporting_service.util.PdfLayout;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.system.common_library.dto.report.TransactionReportDTO;
import com.system.common_library.dto.report.TransactionReportRequest;
import com.system.common_library.enums.ReportType;
import com.system.common_library.enums.State;
import com.system.common_library.service.TransactionDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionReportService {
    @Autowired
    private ReportStatusService reportStatusService;

    @DubboReference
    private TransactionDubboService transactionDubboService;

    public byte[] createTransactionsReportByFilter(TransactionsFilterRequest request) throws Exception {
        TransactionReportRequest gRpcRequest = TransactionReportRequest.builder()
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .minAmount(request.getMinAmount())
                .maxAmount(request.getMaxAmount())
                .transactionType(request.getTransactionType())
                .transactionStatus(request.getTransactionStatus())
                .senderAccountNumber(request.getSenderAccountNumber())
                .recipientAccountNumber(request.getRecipientAccountNumber())
                .build();
        Report report = reportStatusService.createReport(ReportType.TRANSACTION);
        List<TransactionReportDTO> transactions = Optional.ofNullable(
                transactionDubboService.getTransactionByFilter(gRpcRequest)
        ).orElseThrow(() -> new Exception("error.dubbo.service_unavailable"));
        byte[] pdfFile = generateTransactionsReportPdf(transactions);
        reportStatusService.updateReportStatus(report.getId(), State.COMPLETED);
        return pdfFile;
    }

    private byte[] generateTransactionsReportPdf(List<TransactionReportDTO> transactions) throws IOException, IllegalAccessException {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);
            pdfDocument.setDefaultPageSize(PageSize.A4.rotate());
            Document document = new Document(pdfDocument);
            PdfLayout.createHeader(document);
            PdfLayout.addReportTitle(document, ReportTitle.TRANSACTION_REPORT);
            PdfLayout.generateTableFromDTOList(document, null, transactions);
            PdfLayout.createFooter(document);
            document.close();
            return outputStream.toByteArray();
        }
    }

}
