package com.example.notification_service.controller;

import com.example.notification_service.domain.dto.NotificationStatusDashboardDTO;
import com.example.notification_service.domain.dto.TrackingNotificationDTO;
import com.example.notification_service.service.interfaces.TrackingNotificationService;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/tracking")
@RequiredArgsConstructor
public class TrackingController {
    private final TrackingNotificationService trackingNotificationService;

    @GetMapping("/trackingWithoutPagination")
    public ResponseEntity<List<TrackingNotificationDTO>> getTrackingNotificationWithoutPagination() {
        return ResponseEntity.ok(trackingNotificationService.getTrackingNotificationsWithoutPagination());
    }
    @GetMapping("/tracking")
    public ResponseEntity<Page<TrackingNotificationDTO>> getTrackingNotification(@Nullable Pageable pageable) {
        return ResponseEntity.ok(trackingNotificationService.getTrackingNotifications(pageable));
    }
    @GetMapping("/dashboard-info")
    public ResponseEntity<NotificationStatusDashboardDTO> getNotificationStatusDashboard() {
        return ResponseEntity.ok(trackingNotificationService.getNotificationStatusDashboard());
    }
}
