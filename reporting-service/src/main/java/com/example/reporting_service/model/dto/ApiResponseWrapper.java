package com.example.reporting_service.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponseWrapper<T> {

    @Schema(description = "HTTP status code of the response", example = "201")
    private int status;

    @Schema(description = "Message accompanying the status code", example = "Create successfully")
    private String message;

    @Schema(description = "Data returned in the response")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;
}
