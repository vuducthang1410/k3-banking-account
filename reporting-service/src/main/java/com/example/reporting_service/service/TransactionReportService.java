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
    private ReportService reportService;

    @Autowired
    private FilebaseStorageService filebaseStorageService;

    @DubboReference
    private TransactionDubboService transactionDubboService;

    public String createTransactionsReportByFilter(TransactionsFilterRequest request) throws Exception {
        // 1. Tạo report với trạng thái PENDING
        Report report = reportService.createReport(ReportType.TRANSACTION_LIST, request.getCustomerId());

        try {
            // 2. Chuẩn bị request cho gRPC
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

            // 3. Gọi gRPC lấy danh sách giao dịch
            List<TransactionReportDTO> transactions = Optional.ofNullable(
                    transactionDubboService.getTransactionByFilter(gRpcRequest)
            ).orElseThrow(() -> new Exception("error.dubbo.service_unavailable"));

            // 4. Tạo file PDF từ danh sách giao dịch
            byte[] pdfFile = generateTransactionsReportPdf(transactions);

            // 5. Upload file PDF lên Filebase
            String fileName = PdfLayout.generateReportFileName(ReportType.TRANSACTION_LIST);
            String fileUrl = filebaseStorageService.uploadFile(pdfFile, fileName);

            // 6. Cập nhật trạng thái báo cáo & lưu URL vào DB
            reportService.updateReportStatus(report.getId(), State.COMPLETED, fileUrl);

            // 7. Trả về URL file PDF
            return fileUrl;
        } catch (Exception e) {
            // Nếu có lỗi, cập nhật trạng thái FAILED
            reportService.updateReportStatus(report.getId(), State.FAILED, null);
            throw new Exception("error.report.generation_failed", e);
        }
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
