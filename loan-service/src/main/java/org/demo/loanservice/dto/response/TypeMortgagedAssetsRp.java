package org.demo.loanservice.dto.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class TypeMortgagedAssetsRp implements Serializable {
    private String id;
    private String name;
    private String description;
    private Boolean isActive;
    private String assetType;
    private String assetStatus;
    private String createdDate;
}
