package org.demo.loanservice.common;

public class MessageValue {

    // Validation messages for DTOs
    public static final String VALID_DTO_NAME_NOT_BLANK = "valid.dto.name.notBlank";
    public static final String VALID_DTO_DESCRIPTION_NOT_BLANK = "valid.dto.description.notBlank";
    public static final String VALID_DTO_UNIT_NOT_VALID = "valid.dto.unit.notValid";
    public static final String VALID_DTO_INTEREST_RATE_IS_POSITIVE = "{valid.dto.interest_rate.isPositive}";
    public static final String STATUS_CODE_SUCCESSFULLY = "00000";
    public static final String STATUS_CODE_BAD_REQUEST="40000";
    public static final String STATUS_CODE_SERVER_ERROR="50000";
    public static final String DUBBO_SERVICE_ERROR="50001";
    // Transaction content keys
    public static final String CONTENT_TRANSACTION_PRINCIPAL = "content.transaction.principal";
    public static final String CONTENT_TRANSACTION_INTEREST = "content.transaction.interest";
    public static final String CONTENT_TRANSACTION_PENALTY = "content.transaction.penalty";

}
