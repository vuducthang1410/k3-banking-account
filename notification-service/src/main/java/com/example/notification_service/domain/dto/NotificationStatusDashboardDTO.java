package com.example.notification_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationStatusDashboardDTO {
    long total;
    long success;
    double successRate;
    double failureRate;
    long fail;
    long emailNotification;
    long SMSNotification;
}
