package com.system.common_library.dto.report;

import com.system.common_library.enums.State;
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
public class TransactionReportRequest implements Serializable {
    private String account;
    private LocalDateTime startDate;               // Ngày bắt đầu
    private LocalDateTime endDate;                 // Ngày kết thúc
    private Double minAmount;                  // Số tiền tối thiểu
    private Double maxAmount;                  // Số tiền tối đa
    private TransactionType transactionType;   // Loại giao dịch (Nội bộ, Liên ngân hàng, thanh toán)
    private State transactionStatus;          // Trạng thái giao dịch (Thành công, Thất bại, v.v.)
    private String senderAccountNumber;        // Tài khoản người gửi
    private String recipientAccountNumber;     // Tài khoản người nhận
}
