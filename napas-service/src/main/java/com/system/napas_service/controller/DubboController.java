package com.system.napas_service.controller;

import com.system.common_library.dto.transaction.account.credit.CreateCreditPaymentTransactionDTO;
import com.system.common_library.dto.transaction.account.credit.CreateCreditTransactionDTO;
import com.system.common_library.service.TransactionDubboService;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class DubboController {

    @DubboReference
    private final TransactionDubboService transactionDubboService;

    @PostMapping("/1")
    public ResponseEntity<?> getListByFilter1(@Parameter CreateCreditTransactionDTO create) {

        try {

            return ResponseEntity.status(HttpStatus.OK).body(transactionDubboService.createCreditTransaction(create));
        }catch (Exception e){

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/1/{id}")
    public ResponseEntity<?> rollback1(@PathVariable(value = "id") String transactionsId) {

        try {

            return ResponseEntity.status(HttpStatus.OK)
                    .body(transactionDubboService.rollbackCreditTransaction(transactionsId));
        }catch (Exception e){

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/2")
    public ResponseEntity<?> getListByFilter2(@Parameter CreateCreditPaymentTransactionDTO create) {

        try {

            return ResponseEntity.status(HttpStatus.OK).body(transactionDubboService.createCreditPaymentTransaction(create));
        }catch (Exception e){

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/2/{id}")
    public ResponseEntity<?> rollback2(@PathVariable(value = "id") String transactionsId) {

        try {

            return ResponseEntity.status(HttpStatus.OK)
                    .body(transactionDubboService.rollbackCreditPaymentTransaction(transactionsId));
        }catch (Exception e){

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
