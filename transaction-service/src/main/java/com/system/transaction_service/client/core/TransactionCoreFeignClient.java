package com.system.transaction_service.client.core;

import com.system.common_library.dto.request.transaction.CoreTransactionRollbackDTO;
import com.system.common_library.dto.request.transaction.CreateExternalTransactionDTO;
import com.system.common_library.dto.request.transaction.CreateInternalTransactionDTO;
import com.system.common_library.dto.request.transaction.CreateSystemTransactionDTO;
import com.system.common_library.dto.response.transaction.TransactionCoreNapasDTO;
import com.system.transaction_service.client.core.fallback.TransactionCoreFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        value = "core-transaction-client",
        url = "${core.banking.service.url}/transactions",
        fallback = TransactionCoreFeignClientFallback.class
)
public interface TransactionCoreFeignClient {

    // Create external transaction in Core banking service
    @PostMapping("/external")
    ResponseEntity<TransactionCoreNapasDTO> createExternal(@RequestBody @Validated CreateExternalTransactionDTO create);

    // Create internal transaction in Core banking service
    @PostMapping("/internal")
    ResponseEntity<TransactionCoreNapasDTO> createInternal(@RequestBody @Validated CreateInternalTransactionDTO create);

    // Create system transaction in Core banking service
    @PostMapping("/system")
    ResponseEntity<TransactionCoreNapasDTO> createSystem(@RequestBody @Validated CreateSystemTransactionDTO create);

    // Rollback transaction in Core banking service
    @PostMapping(value = "/rollback")
    ResponseEntity<String> rollback(@RequestBody @Validated CoreTransactionRollbackDTO rollback);
}
