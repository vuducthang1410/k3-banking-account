package org.demo.loanservice.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.demo.loanservice.common.MessageValue;
import org.demo.loanservice.validatedCustom.interfaceValidate.InterestRateValidation;
import org.demo.loanservice.validatedCustom.interfaceValidate.UnitValidation;

import java.math.BigDecimal;

@Schema(description = "Request object for Interest Rate details")
@Data
public class InterestRateRq {

    @Schema(description = "Interest rate value in percentage", example = "7.5")
    @InterestRateValidation(message = MessageValue.VALID_DTO_INTEREST_RATE_IS_POSITIVE)
    private Double interestRate;

    @Schema(description = "Unit of time for the interest rate", example = "DATE , MONTH or YEAR")
    @UnitValidation(message = "{valid.dto.unit.notValid}")
    private String unit;
    @Min(value = 0)
    private BigDecimal minimumAmount;
    @Min(value = 0)
    private Integer minimumLoanTerm;
    @NotBlank
    private String loanProductId;

}
