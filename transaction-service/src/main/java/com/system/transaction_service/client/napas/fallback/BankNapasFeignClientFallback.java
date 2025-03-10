package com.system.transaction_service.client.napas.fallback;

import com.system.transaction_service.client.napas.BankNapasFeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class BankNapasFeignClientFallback implements BankNapasFeignClient {

    @Override
    public ResponseEntity<?> getList
            (String search, Boolean isAvailable, String sort, Integer page, Integer limit) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }

    @Override
    public ResponseEntity<?> getById(String id) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }
}
