package com.example.smsHost.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequestDTO {
    private String message;
    private String senderPhonenumber;
    private String receiverPhonenumber;
}
