package com.system.common_library.dto.report;

import com.system.common_library.dto.report.config.FieldName;
import com.system.common_library.dto.response.loan.PaymentScheduleRp;
import com.system.common_library.enums.FormDeftRepaymentEnum;
import com.system.common_library.enums.LoanStatus;
import com.system.common_library.enums.Unit;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanReportResponse implements Serializable {

    private String customerId;

    @FieldName("Mã khoản vay")
    private String loanId;

    @FieldName("Số tiền vay")
    private Double loanAmount;

    @FieldName("Hình thức trả nợ")
    private FormDeftRepaymentEnum loanType;

    @FieldName("Ngày bắt đầu vay")
    private LocalDate startDate;

    @FieldName("Trạng thái khoản vay")
    private LoanStatus loanStatus;

    @FieldName("Số nợ còn lại")
    private Double remainingBalance;

    @FieldName("Lịch thanh toán của khoản vay dự kiến")
    private List<PaymentScheduleRp> paymentScheduleList;

    @FieldName("Lãi suất")
    private Double interestRate;

    private Unit unit;

    private String accountLoanNumber;

}
