package com.system.transaction_service.client.core;

import com.system.common_library.dto.response.PagedDTO;
import com.system.common_library.dto.response.account.AccountCoreDTO;
import com.system.common_library.dto.response.account.AccountExtraCoreDTO;
import com.system.transaction_service.client.core.fallback.AccountCoreFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        value = "core-account-client",
        url = "${core.banking.service.url}/accounts",
        fallback = AccountCoreFeignClientFallback.class
)
public interface AccountCoreFeignClient {

    // Get account list in Core banking service
    @GetMapping("")
    ResponseEntity<PagedDTO<AccountCoreDTO>> getList(@RequestParam(defaultValue = "") String search,
                                                     @RequestParam(defaultValue = "") Boolean isActive,
                                                     @RequestParam(defaultValue = "id,desc") String sort,
                                                     @RequestParam(defaultValue = "0") Integer page,
                                                     @RequestParam(defaultValue = "10") Integer limit);

    // Get account by account number in Core banking service
    @GetMapping("/{account}")
    ResponseEntity<AccountExtraCoreDTO> getByAccountNumber(@PathVariable(value = "account") String account);
}
