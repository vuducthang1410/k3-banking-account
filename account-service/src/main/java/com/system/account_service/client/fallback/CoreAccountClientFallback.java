package com.system.account_service.client.fallback;

import com.system.account_service.client.CoreAccountClient;
import com.system.common_library.dto.request.account.CreateAccountCoreDTO;
import com.system.common_library.dto.response.account.AccountCoreDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

public class CoreAccountClientFallback implements CoreAccountClient {

    @Override
    public ResponseEntity<AccountCoreDTO> create(@RequestBody @Validated CreateAccountCoreDTO create) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}
