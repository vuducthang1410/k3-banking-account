package com.system.transaction_service.client.napas.fallback;

import com.system.common_library.dto.response.PagedDTO;
import com.system.common_library.dto.response.account.AccountExtraNapasDTO;
import com.system.common_library.dto.response.account.AccountNapasDTO;
import com.system.transaction_service.client.napas.AccountNapasFeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class AccountNapasFeignClientFallback implements AccountNapasFeignClient {

    @Override
    public ResponseEntity<PagedDTO<AccountNapasDTO>> getList(String search, Boolean isActive, String sort, Integer page, Integer limit) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }

    @Override
    public ResponseEntity<AccountExtraNapasDTO> getByAccountNumber(String account) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }
}
