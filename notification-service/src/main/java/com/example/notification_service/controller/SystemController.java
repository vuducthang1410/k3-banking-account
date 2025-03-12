package com.example.notification_service.controller;

import com.example.notification_service.domain.entity.BalanceFluctuation;
import com.example.notification_service.service.interfaces.SystemService;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sys")
@RequiredArgsConstructor
public class SystemController {
    private final SystemService systemService;
    @GetMapping("balance_fluctuation")
    public ResponseEntity<Page<BalanceFluctuation>> getAll(@Nullable Pageable pageable){
        if (pageable == null || pageable.isUnpaged()) {
            pageable = PageRequest.of(0, 10, Sort.by("id").descending());
        }
        return ResponseEntity.ok(systemService.getBalanceFluctuation(pageable));
    }
}
