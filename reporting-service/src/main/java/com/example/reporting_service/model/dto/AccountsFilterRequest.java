package com.example.reporting_service.model.dto;

import com.system.common_library.enums.AccountType;
import com.system.common_library.enums.ObjectStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountsFilterRequest {

    @NotBlank(message = "customerId.required")
    private String customerId;

    @NotNull(message = "accountType.required")
    private AccountType accountType;

    private String bankBranch;

    @Min(value = 0, message = "startBalance.min")
    private Double startBalance;

    @Min(value = 0, message = "endBalance.min")
    private Double endBalance;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @NotNull(message = "startAt.required")
    private LocalDate startAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @NotNull(message = "endAt.required")
    private LocalDate endAt;

    private ObjectStatus status;
}
