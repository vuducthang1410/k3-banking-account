package com.example.notification_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MessageDTO {
    private String message;
    private String senderPhonenumber;
    private String receiverPhonenumber;
    private LocalDateTime sendTime;
}
