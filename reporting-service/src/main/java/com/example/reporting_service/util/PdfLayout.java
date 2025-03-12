package com.example.reporting_service.util;

import com.example.reporting_service.model.Enum.ReportTitle;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.system.common_library.dto.report.config.FieldName;
import com.system.common_library.enums.ReportType;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class PdfLayout {

    public static PdfFont getFont() {
        try {
            ClassPathResource fontResource = new ClassPathResource("fonts/arial-unicode-ms.ttf");
            try (InputStream fontStream = fontResource.getInputStream()) {
                return PdfFontFactory.createFont(fontStream.readAllBytes(), "Identity-H");
            }
        } catch (IOException e) {
            throw new RuntimeException("Không thể load font PDF", e);
        }
    }

    public static void createHeader(Document document) throws IOException {
        PdfFont font = getFont();

        // Lấy logo từ resources và set vào báo cáo
        Resource logoResource = new ClassPathResource("images/logo.png");
        Path path = logoResource.getFile().toPath();
        byte[] logoBytes = Files.readAllBytes(path);

        Table headerTable = new Table(new float[]{7, 3});
        headerTable.setWidth(UnitValue.createPercentValue(100));
        Cell logoCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        if (logoBytes != null && logoBytes.length > 0) {
            Image logo = new Image(ImageDataFactory.create(logoBytes)).setWidth(210);
            logoCell.add(logo);
        }
        logoCell.add(new Paragraph("NGÂN HÀNG THƯƠNG MẠI CỔ PHẦN KIÊN LONG")
                .setFont(font)
                .setFontSize(9)
                .setTextAlignment(TextAlignment.LEFT));
        headerTable.addCell(logoCell);

        Cell timeCell = new Cell()
                .add(new Paragraph("Thời gian tạo:").setFont(font).setFontSize(10))
                .add(new Paragraph(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).setFont(font).setFontSize(10))
                .setTextAlignment(TextAlignment.RIGHT)
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE);
        headerTable.addCell(timeCell);
        document.add(headerTable);
        document.add(new Paragraph("\n"));
    }

    public static void createFooter(Document document) {
        PdfFont font = getFont();

        document.add(new Paragraph("\n\n"));
        String footerText = "Địa chỉ ngân hàng: 123 Đường XYZ, Quận 1, TP.HCM,\n" +
                "Số điện thoại hotline: 1900 123 456, " + "Email hỗ trợ: support@kienlongbank.com\n" +
                "Website: www.kienlongbank.com";
        Paragraph footerParagraph = new Paragraph(footerText)
                .setFont(font)
                .setFontSize(8)
                .setTextAlignment(TextAlignment.CENTER);
        float yPosition = document.getPdfDocument().getPage(1).getPageSize().getTop() - 820;
        document.showTextAligned(footerParagraph, 297, yPosition, TextAlignment.CENTER);
    }

    public static void generateTableFromDTOList(Document document, ReportTitle reportTitle, Object data) throws IllegalAccessException {
        PdfFont font = getFont();

        if (data instanceof List<?> dataList && !dataList.isEmpty()) {
            Object firstItem = dataList.get(0);
            Class<?> itemType = firstItem.getClass();

            // Lọc các trường có @FieldName
            List<Field> fieldsWithAnnotation = Arrays.stream(itemType.getDeclaredFields())
                    .filter(f -> f.isAnnotationPresent(FieldName.class))
                    .toList();

            if (!fieldsWithAnnotation.isEmpty()) {
                int totalColumns = fieldsWithAnnotation.size();
                float[] columnWidths = new float[totalColumns];
                Arrays.fill(columnWidths, 100f / totalColumns);  // Điều chỉnh chiều rộng cột tự động

                Table subTable = new Table(UnitValue.createPercentArray(columnWidths));
                subTable.setWidth(UnitValue.createPercentValue(100));
                subTable.setKeepTogether(true);

                // **Thêm tiêu đề bảng (nếu có)**
                if (reportTitle != null) {
                    document.add(new Paragraph(reportTitle.getTitle()) // Lấy tiêu đề từ enum
                            .setFont(font).setBold().setFontSize(12)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setMarginBottom(5));
                }

                // Thêm tiêu đề cột
                for (Field subField : fieldsWithAnnotation) {
                    FieldName subAnnotation = subField.getAnnotation(FieldName.class);
                    subTable.addCell(new Cell()
                            .add(new Paragraph(subAnnotation.value())
                                    .setFont(font).setBold().setFontSize(9))
                            .setBorder(new SolidBorder(1))
                            .setTextAlignment(TextAlignment.CENTER)
                            .setBackgroundColor(new DeviceRgb(230, 230, 230))
                            .setPadding(2));
                }

                // Thêm dữ liệu từ danh sách DTO
                for (Object obj : dataList) {
                    for (Field subField : fieldsWithAnnotation) {
                        subField.setAccessible(true);
                        Object subValue = subField.get(obj);

                        if (subValue instanceof LocalDate) {
                            subValue = ((LocalDate) subValue).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                        } else if (subValue instanceof LocalDateTime) {
                            subValue = ((LocalDateTime) subValue).format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
                        }

                        subTable.addCell(new Cell()
                                .add(new Paragraph(subValue != null ? subValue.toString() : "N/A")
                                        .setFont(font).setFontSize(9))
                                .setBorder(new SolidBorder(1))
                                .setPadding(2)
                                .setTextAlignment(TextAlignment.LEFT)
                                .setKeepTogether(true));
                    }
                }

                document.add(subTable);
            }
        }
    }

    public static void populateFromSingleDto(Document document, Object data) throws IllegalAccessException {
        PdfFont font = getFont();
        Table infoTable = new Table(new float[]{1});
        infoTable.setWidth(UnitValue.createPercentValue(100));
        infoTable.setBorder(Border.NO_BORDER);

        for (Field field : data.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            FieldName annotation = field.getAnnotation(FieldName.class);
            if (annotation != null) {
                Object value = field.get(data);
                if (!(value instanceof List<?>)) {
                    infoTable.addCell(new Cell()
                            .add(new Paragraph(annotation.value() + ": " + (value != null ? value.toString() : "N/A"))
                                    .setFont(font).setFontSize(12)
                                    .setTextAlignment(TextAlignment.LEFT)) // Căn trái
                            .setBorder(Border.NO_BORDER));
                }
            }
        }

        document.add(infoTable);
    }

    public static void addReportTitle(Document document, ReportTitle reportTitle) {
        PdfFont font = getFont();
        document.add(new Paragraph(reportTitle.getTitle())
                .setFont(font)
                .setFontSize(18)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));
    }

    public static String generateReportFileName(ReportType reportType) {
        // Lấy thời gian hiện tại theo định dạng yyyyMMdd_HHmmss
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        // Ví dụ: account_report_20250311_184530.pdf
        return reportType.toString().toLowerCase() + "_report_" + timestamp + ".pdf";
    }

}
