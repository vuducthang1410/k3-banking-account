package com.example.notification_service.domain.dto;

import com.example.notification_service.domain.enumValue.Channel;
import com.example.notification_service.domain.enumValue.Status;
import com.example.notification_service.domain.enumValue.Template;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TrackingNotificationDTO {
    private Status status;
    private String description;
    private Template template;
    private Channel channel;
}
