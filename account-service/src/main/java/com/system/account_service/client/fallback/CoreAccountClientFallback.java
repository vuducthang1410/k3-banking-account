package com.system.account_service.client.fallback;

import com.system.account_service.client.CoreAccountClient;
import com.system.common_library.dto.request.account.CreateAccountCoreDTO;
import com.system.common_library.dto.request.account.UpdateAccountCoreDTO;
import com.system.common_library.dto.response.account.AccountCoreDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public class CoreAccountClientFallback implements CoreAccountClient {

    @Override
    public ResponseEntity<AccountCoreDTO> getByAccountNumber(@PathVariable(value = "account") String account) {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<AccountCoreDTO> create(@RequestBody @Validated CreateAccountCoreDTO create) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @Override
    public ResponseEntity<AccountCoreDTO> update(@PathVariable(value = "account") String account,
                             @RequestBody @Validated UpdateAccountCoreDTO update) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @Override
    public ResponseEntity<?> delete(@PathVariable(value = "id") String id) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }
}
