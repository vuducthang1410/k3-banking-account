package org.demo.loanservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request object for debt repayment")
public class DeftRepaymentRq {

    @NotBlank
    @Schema(description = "Unique identifier of the payment schedule", example = "ps-987654")
    private String paymentScheduleId;

    @Schema(description = "Type of payment", example = "FULL_PAYMENT")
    private String paymentType;
}
