package org.demo.loanservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LegalDocumentRp implements Serializable {
    private String legalDocumentId;
    private String description;
    private String imageBase64;
    private String documentGroupId;
    private String documentType;
    private String requestStatus;
    private String expirationDate;
}
