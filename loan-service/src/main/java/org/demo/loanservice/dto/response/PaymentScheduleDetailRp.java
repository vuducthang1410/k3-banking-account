package org.demo.loanservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentScheduleDetailRp {
    private String paymentScheduleId;
    private String nameSchedule;
    private String dueDate;
    private String amountInterest;
    private String amountLoan;
    private String amountFined;
    private boolean isPaidInterest;
    private boolean isPaidLoan;
    private boolean isPaidFined;
    private String deftRepaymentStatus;
    private boolean isEnd;
    private String paymentDate;
}
