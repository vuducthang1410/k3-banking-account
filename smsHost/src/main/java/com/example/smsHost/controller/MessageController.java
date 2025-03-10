package com.example.smsHost.controller;

import com.example.smsHost.domain.dto.MessageDTO;
import com.example.smsHost.domain.dto.MessageRequestDTO;
import com.example.smsHost.sevice.MessagesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/sms")
public class MessageController {
    private final MessagesService messagesService;
    public MessageController(MessagesService messagesService) {
        this.messagesService = messagesService;
    }
    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendSms(@RequestBody MessageRequestDTO request) {
        log.info(request.toString());
        MessageDTO sentMessage = messagesService.saveMessage(request);
        log.info(sentMessage.toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(sentMessage);
    }
//    public void sendSms(@RequestBody MessageRequestDTO request) {
//        log.info(request.toString());
//        MessageDTO sentMessage = messagesService.saveMessage(request);
//        log.info(sentMessage.toString());
////        return ResponseEntity.status(HttpStatus.CREATED).body(sentMessage);
//    }
    @GetMapping("/sent")
    public ResponseEntity<List<MessageDTO>> getSentMessages() {
        return ResponseEntity.status(HttpStatus.OK).body(messagesService.getSentMessages());
    }
}
