package com.system.common_library.dto.report;

import com.system.common_library.dto.report.config.FieldName;
import com.system.common_library.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionReportDTO implements Serializable {

    @FieldName("Mã giao dich")
    private String transactionId;

    private String account;

    private String customerId;

    @FieldName("Số tài khoản người gửi")
    private String senderAccountNumber;      // Số tài khoản người gửi

    @FieldName("Ngày giao dịch")
    private String recipientAccountNumber;   // Số tài khoản người nhận

    @FieldName("Ngày giao dịch")
    private LocalDateTime transactionDate;       // Ngày giao dịch

    @FieldName("Số tiền giao dịch")
    private Double amount;        // Số tiền giao dịch

    @FieldName("Loại giao dịch")
    private TransactionType transactionType;   // Loại giao dịch (Nội bộ, Liên ngân hàng, thanh toán)

    @FieldName("Trạng thái giao dịch")
    private String status;         // Trạng thái giao dịch (SUCCESS, PENDING, FAILED,...)

    @FieldName("Phí giao dịch")
    private Double fee;           // Phí giao dịch

    @FieldName("Mô tả giao dịch")
    private String description;              // Mô tả chi tiết giao dịch
}
