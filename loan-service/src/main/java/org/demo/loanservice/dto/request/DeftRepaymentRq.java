package org.demo.loanservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeftRepaymentRq {
    @NotBlank
    private String paymentScheduleId;
    private String paymentType;
}
