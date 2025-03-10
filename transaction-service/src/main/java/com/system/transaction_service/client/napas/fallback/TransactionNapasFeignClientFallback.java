package com.system.transaction_service.client.napas.fallback;

import com.system.common_library.dto.request.transaction.CreateNapasTransactionDTO;
import com.system.common_library.dto.request.transaction.NapasTransactionRollbackDTO;
import com.system.common_library.dto.response.transaction.TransactionCoreNapasDTO;
import com.system.transaction_service.client.napas.TransactionNapasFeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class TransactionNapasFeignClientFallback implements TransactionNapasFeignClient {

    @Override
    public ResponseEntity<TransactionCoreNapasDTO> createTransaction(CreateNapasTransactionDTO create) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }

    @Override
    public ResponseEntity<String> rollbackTransaction(NapasTransactionRollbackDTO rollback) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }
}
