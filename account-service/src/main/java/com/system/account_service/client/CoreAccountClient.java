package com.system.account_service.client;

import com.system.account_service.client.fallback.CoreAccountClientFallback;
import com.system.common_library.dto.request.account.CreateAccountCoreDTO;
import com.system.common_library.dto.request.account.UpdateAccountCoreDTO;
import com.system.common_library.dto.response.account.AccountCoreDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        value = "core-account-client",
        url = "${service.core.banking.url}/accounts",
        fallback = CoreAccountClientFallback.class
)
public interface CoreAccountClient {

    // Todo: Get account By account_number
    @GetMapping("{account}")
    ResponseEntity<AccountCoreDTO> getByAccountNumber(@PathVariable(value = "account") String account);

    // Todo: Call api create account in CoreBanking
    @PostMapping("")
    ResponseEntity<AccountCoreDTO> create(@RequestBody @Validated CreateAccountCoreDTO create);

    @PutMapping("{account}")
    ResponseEntity<AccountCoreDTO> update(@PathVariable(value = "account") String account,
                                    @RequestBody @Validated UpdateAccountCoreDTO update);

    // Todo: Call api delete account in CoreBanking
    @DeleteMapping("{id}")
    ResponseEntity<?> delete(@PathVariable(value = "id") String id);
}
