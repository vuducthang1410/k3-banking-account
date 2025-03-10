package com.example.notification_service.controller;

import com.example.notification_service.domain.dto.LoginRequestDTO;
import com.example.notification_service.domain.dto.LoginResponseDTO;
import com.example.notification_service.domain.entity.BalanceFluctuation;
import com.example.notification_service.service.interfaces.SystemService;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request){

        if(request.getUsername() == null || request.getPassword() == null||request.getUsername().isEmpty() || request.getPassword().isEmpty()){
            return ResponseEntity.badRequest().build();
        }
        if(request.getUsername().equals("admin") && request.getPassword().equals("password")){
            return ResponseEntity.ok(LoginResponseDTO.builder().token("fake-jwt-token").status("Login Successfully").build());
        }
        return ResponseEntity.notFound().build();
    }
}
