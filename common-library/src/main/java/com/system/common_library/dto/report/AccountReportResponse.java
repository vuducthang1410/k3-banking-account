package com.system.common_library.dto.report;

import com.system.common_library.dto.report.config.FieldName;
import com.system.common_library.enums.AccountType;
import com.system.common_library.enums.ObjectStatus;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountReportResponse implements Serializable {
    private String customerId;

    @FieldName("Số tài khoản")
    private String accountNumber;

    @FieldName("Loại tài khoản")
    private AccountType accountType;

    @FieldName("Trạng thái tài khoản")
    private ObjectStatus status;

    @FieldName("Chi nhánh ngân hàng")
    private String bankBranch;

    @FieldName("Số dư")
    private Double balance;

    @FieldName("Ngày mở tài khoản")
    private LocalDateTime openedAt;

    @FieldName("Hạn mức tín dụng")
    private String creditLimit;

    @FieldName("Số dư nợ")
    private String debtBalance;

    @FieldName("Lãi xuất")
    private Double rate;

    @FieldName("Chu kỳ thanh toán")
    private Integer billingCycle;
}


