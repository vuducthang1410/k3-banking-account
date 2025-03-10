package com.system.account_service.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponseDTO implements Serializable {
    @Schema(description = "Error code", example = "400")
    private int code;

    @Schema(description = "Message accompanying", example = "Invalid account info")
    private String message;

    @Schema(description = "List errors description")
    private List<String> errors;
}
