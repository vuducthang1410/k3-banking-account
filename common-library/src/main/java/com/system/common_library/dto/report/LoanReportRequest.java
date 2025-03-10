package com.system.common_library.dto.report;

import com.system.common_library.enums.FormDeftRepaymentEnum;
import com.system.common_library.enums.LoanStatus;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanReportRequest implements Serializable {
    private String loanId;                 // ID Khoản vay
    private String customerId;             // ID Khách hàng
    private Double minLoanAmount;      // Số tiền vay (từ)
    private Double maxLoanAmount;      // Số tiền vay (đến)
    private FormDeftRepaymentEnum loanType;               // Loại khoản vay
    private LocalDate startDate;
    private LocalDate endDate;
    private LoanStatus loanStatus;             // Trạng thái khoản vay
}
