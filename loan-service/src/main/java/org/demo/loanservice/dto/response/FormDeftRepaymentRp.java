package org.demo.loanservice.dto.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FormDeftRepaymentRp implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String formId;
    private String formName;
    private Boolean isActive;
    private String code;
}
