package com.system.common_library.dto.response.loan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.system.common_library.dto.report.config.FieldName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentScheduleRp {
    private String paymentScheduleId;
    @FieldName("Tên trả khoản vay")
    private String nameSchedule;
    @FieldName("Ngày trả dự kiến")
    private String dueDate;
    @FieldName("Số tiền trả")
    private String amountRemaining;
    @FieldName("Trạng thái")
    private String status;
    private String isPaid;
}
