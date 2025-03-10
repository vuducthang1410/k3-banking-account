package org.demo.loanservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CICResponse {
    @JsonProperty("cccd")
    private String cccd;

    @JsonProperty("credit_score")
    private int creditScore;

    @JsonProperty("credit_rating")
    private String creditRating;

    @JsonProperty("debt_status")
    private String debtStatus;

    @JsonProperty("last_updated")
    private String lastUpdated;
}
