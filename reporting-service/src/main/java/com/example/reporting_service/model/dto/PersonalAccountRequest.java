package com.example.reporting_service.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.system.common_library.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PersonalAccountRequest {

    @NotNull(message = "customerId cannot be null")
    private String customerId;

    @NotNull(message = "account cannot be null")
    private String account;

    @NotNull(message = "accountType cannot be null")
    private AccountType accountType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startTransactionDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endTransactionDate;

}
