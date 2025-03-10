package com.example.notification_service.service.interfaces;

import com.example.notification_service.domain.dto.NotificationStatusDashboardDTO;
import com.example.notification_service.domain.dto.TrackingNotificationDTO;
import com.example.notification_service.domain.entity.NotificationTemplate;
import com.example.notification_service.domain.entity.TrackingNotification;
import com.example.notification_service.domain.enumValue.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TrackingNotificationService {
    TrackingNotification saveTracking(TrackingNotification trackingNotification);
    TrackingNotification saveTracking(NotificationTemplate template, Status status, String description);
    Page<TrackingNotificationDTO> getTrackingNotifications(Pageable pageable);
    List<TrackingNotificationDTO> getTrackingNotificationsWithoutPagination();
    NotificationStatusDashboardDTO getNotificationStatusDashboard();
}
