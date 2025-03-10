package com.example.reporting_service.model.dto;

import com.system.common_library.enums.FormDeftRepaymentEnum;
import com.system.common_library.enums.LoanStatus;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoansFilterRequest {
    private String loanId;
    private String customerId;
    private Double minLoanAmount;
    private Double maxLoanAmount;

    private FormDeftRepaymentEnum loanType;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startAccountDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endAccountDate;

    private LoanStatus loanStatus;
}
