package com.example.notification_service.service;

import com.example.notification_service.domain.dto.NotificationStatusDashboardDTO;
import com.example.notification_service.domain.dto.TrackingNotificationDTO;
import com.example.notification_service.domain.entity.NotificationTemplate;
import com.example.notification_service.domain.entity.TrackingNotification;
import com.example.notification_service.domain.enumValue.Channel;
import com.example.notification_service.domain.enumValue.Status;
import com.example.notification_service.repository.TrackingNotificationRepository;
import com.example.notification_service.service.interfaces.TrackingNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class TrackingNotificationServiceImpl implements TrackingNotificationService {

    private final TrackingNotificationRepository trackingNotificationRepository;
    @Override
    public TrackingNotification saveTracking(TrackingNotification trackingNotification) {
        return trackingNotificationRepository.save(trackingNotification);
    }

    @Override
    public TrackingNotification saveTracking(NotificationTemplate template, Status status, String description) {
        if(template == null){
            TrackingNotification trackingNotification =  TrackingNotification.builder()
                    .status(status)
                    .description(description)
                    .build();
            return trackingNotificationRepository.save(trackingNotification);
        }
        TrackingNotification trackingNotification =  TrackingNotification.builder()
                .status(status)
                .description(description)
                .templateId(template)
                .template(template.getEvent())
                .channel(template.getChannel())
                .build();
        return trackingNotificationRepository.save(trackingNotification);
    }

    @Override
    public Page<TrackingNotificationDTO> getTrackingNotifications(Pageable pageable) {
        Page<TrackingNotification> trackingNotifications = trackingNotificationRepository.findAll(pageable);
        return trackingNotifications.map(this::convertToDTO);
    }

    @Override
    public List<TrackingNotificationDTO> getTrackingNotificationsWithoutPagination() {
        Iterable<TrackingNotification> trackingNotifications = trackingNotificationRepository.findAll();

        return StreamSupport.stream(trackingNotifications.spliterator(), false)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotificationStatusDashboardDTO getNotificationStatusDashboard() {
        long total = trackingNotificationRepository.count();
        long success = trackingNotificationRepository.countByStatus(Status.Success);
        long fail = trackingNotificationRepository.countByStatus(Status.Fail);
        long sms = trackingNotificationRepository.countByChannel(Channel.SMS);
        long email = trackingNotificationRepository.countByChannel(Channel.EMAIL);
        return NotificationStatusDashboardDTO.builder()
                .total(total)
                .emailNotification(email)
                .SMSNotification(sms)
                .success(success)
                .fail(fail)
                .successRate((double) success /total)
                .failureRate((double) fail /total)
                .build();
    }

    private TrackingNotificationDTO convertToDTO(TrackingNotification notification) {
        return TrackingNotificationDTO.builder()
                .description(notification.getDescription())
                .status(notification.getStatus())
                .template(notification.getTemplate())
                .channel(notification.getChannel())
                .build();
    }
}
