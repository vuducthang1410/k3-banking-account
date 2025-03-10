package com.example.notification_service.domain.dto;

import com.example.notification_service.domain.enumValue.Channel;
import com.example.notification_service.domain.enumValue.Template;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplateResponseDTO {
    private int id;
    private Template event;
    private String title;
    private String content;
    private Channel channel;
    private String description;
}
