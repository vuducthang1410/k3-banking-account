package com.example.notification_service.client;

import com.example.notification_service.client.fallback.SMSFeignClientFallback;
import com.example.notification_service.domain.dto.MessageDTO;
import com.example.notification_service.domain.dto.MessageRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        value = "sms-host-client",
        url = "${sms.service.url}/sms",
        fallback = SMSFeignClientFallback.class
)
public interface SMSFeignClient {
    @PostMapping("/send")
    ResponseEntity<MessageDTO> sendSms(@RequestBody MessageRequestDTO request) ;
    @GetMapping("/sent")
    ResponseEntity<List<MessageDTO>> getSentMessages() ;
}