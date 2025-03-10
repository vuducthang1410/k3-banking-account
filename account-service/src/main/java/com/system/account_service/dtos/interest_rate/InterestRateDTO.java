package com.system.account_service.dtos.interest_rate;

import com.system.account_service.entities.type.InterestGroup;
import com.system.account_service.utils.MessageKeys;
import com.system.account_service.validator.annotation.EnumValidator;
import com.system.account_service.validator.annotation.MaxTermSavingValidator;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterestRateDTO {
    @DecimalMin(value = "0.0", message = MessageKeys.MESSAGES_SCOPE_INTEREST_RATE)
    @NotNull(message = MessageKeys.MESSAGES_BLANK_INTEREST_RATE)
    private BigDecimal rate;

//    @EnumValidator(enumClass = Unit.class, message = MessageKeys.MESSAGES_ENUM_UNIT)
//    @NotBlank(message = MessageKeys.MESSAGES_BLANK_UNIT)
//    @Getter(AccessLevel.NONE)
//    private String unit;

    @EnumValidator(enumClass = InterestGroup.class, message = MessageKeys.MESSAGES_ENUM_UNIT)
    @NotBlank(message = MessageKeys.MESSAGES_BLANK_INTEREST_RATE_GROUP)
    @Getter(AccessLevel.NONE)
    private String group;

    @Min(value = 1, message = MessageKeys.MESSAGES_SCOPE_MIN_TERM)
    private Integer minimumTerm;

    @MaxTermSavingValidator
    private Integer maximumTerm;

    private Boolean isActive;

//    public String getUnit() {
//        return unit.toUpperCase();
//    }

    public String getGroup() {
        return group.toUpperCase();
    }
}
