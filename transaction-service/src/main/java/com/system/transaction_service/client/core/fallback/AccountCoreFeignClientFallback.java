package com.system.transaction_service.client.core.fallback;

import com.system.common_library.dto.response.PagedDTO;
import com.system.common_library.dto.response.account.AccountCoreDTO;
import com.system.common_library.dto.response.account.AccountExtraCoreDTO;
import com.system.transaction_service.client.core.AccountCoreFeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class AccountCoreFeignClientFallback implements AccountCoreFeignClient {

    @Override
    public ResponseEntity<PagedDTO<AccountCoreDTO>> getList(String search, Boolean isActive, String sort, Integer page, Integer limit) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }

    @Override
    public ResponseEntity<AccountExtraCoreDTO> getByAccountNumber(String account) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }
}
