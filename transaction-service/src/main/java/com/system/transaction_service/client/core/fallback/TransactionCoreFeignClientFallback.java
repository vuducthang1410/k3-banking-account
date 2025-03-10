package com.system.transaction_service.client.core.fallback;

import com.system.common_library.dto.request.transaction.CoreTransactionRollbackDTO;
import com.system.common_library.dto.request.transaction.CreateExternalTransactionDTO;
import com.system.common_library.dto.request.transaction.CreateInternalTransactionDTO;
import com.system.common_library.dto.request.transaction.CreateSystemTransactionDTO;
import com.system.common_library.dto.response.transaction.TransactionCoreNapasDTO;
import com.system.transaction_service.client.core.TransactionCoreFeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class TransactionCoreFeignClientFallback implements TransactionCoreFeignClient {

    @Override
    public ResponseEntity<TransactionCoreNapasDTO> createExternal(CreateExternalTransactionDTO create) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }

    @Override
    public ResponseEntity<TransactionCoreNapasDTO> createInternal(CreateInternalTransactionDTO create) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }

    @Override
    public ResponseEntity<TransactionCoreNapasDTO> createSystem(CreateSystemTransactionDTO create) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }

    @Override
    public ResponseEntity<String> rollback(CoreTransactionRollbackDTO rollback) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }
}
