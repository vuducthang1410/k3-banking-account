package com.system.transaction_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankApiDTO implements Serializable {

    private String code;
    private String desc;
    private List<BankResponse> data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BankResponse implements Serializable {

        private String id;
        private String name;
        private String code;
        private String bin;
        private String shortName;
        private String logo;
        private String transferSupported;
        private String lookupSupported;
        private String short_name;
        private String support;
        private Boolean isTransfer;
        private String swift_code;
    }
}
