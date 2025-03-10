package com.system.transaction_service.client.napas;

import com.system.common_library.dto.response.PagedDTO;
import com.system.common_library.dto.response.account.AccountExtraNapasDTO;
import com.system.common_library.dto.response.account.AccountNapasDTO;
import com.system.transaction_service.client.napas.fallback.AccountNapasFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        value = "napas-account-client",
        url = "${napas.service.url}/accounts",
        fallback = AccountNapasFeignClientFallback.class
)
public interface AccountNapasFeignClient {

    // Get account list in Napas service
    @GetMapping("")
    ResponseEntity<PagedDTO<AccountNapasDTO>> getList(@RequestParam(defaultValue = "") String search,
                                                      @RequestParam(defaultValue = "") Boolean isActive,
                                                      @RequestParam(defaultValue = "id,desc") String sort,
                                                      @RequestParam(defaultValue = "0") Integer page,
                                                      @RequestParam(defaultValue = "10") Integer limit);

    // Get account by account number in Napas service
    @GetMapping("/{account}")
    ResponseEntity<AccountExtraNapasDTO> getByAccountNumber(@PathVariable(value = "account") String account);
}
