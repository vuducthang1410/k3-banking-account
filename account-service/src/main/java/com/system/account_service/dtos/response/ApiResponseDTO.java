package com.system.account_service.dtos.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponseDTO<T> implements Serializable {
    @Schema(description = "Status code", example = "200")
    private int code;

    @Schema(description = "Message accompanying", example = "Get data success!")
    private String message;

    @Schema(description = "Response Payload")
    private T data;
}
