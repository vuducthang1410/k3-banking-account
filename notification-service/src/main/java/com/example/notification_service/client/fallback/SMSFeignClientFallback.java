package com.example.notification_service.client.fallback;

import com.example.notification_service.client.SMSFeignClient;
import com.example.notification_service.domain.dto.MessageDTO;
import com.example.notification_service.domain.dto.MessageRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class SMSFeignClientFallback implements SMSFeignClient {
    @Override
    public ResponseEntity<MessageDTO> sendSms(MessageRequestDTO request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }

    @Override
    public ResponseEntity<List<MessageDTO>> getSentMessages() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN).body(null);
    }
}
