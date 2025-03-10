package org.demo.loanservice.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataResponseWrapper <T>implements Serializable {
    private T data;
    private String message;
    private String status;
    public static DataResponseWrapper<Object> createDataResponseWrapper(Object body, String message, String status){
        return new DataResponseWrapper<>(body,message,status);
    }
}
