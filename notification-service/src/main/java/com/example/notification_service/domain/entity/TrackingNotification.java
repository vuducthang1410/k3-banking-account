package com.example.notification_service.domain.entity;

import com.example.notification_service.domain.enumValue.Channel;
import com.example.notification_service.domain.enumValue.Status;
import com.example.notification_service.domain.enumValue.Template;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "notification_status")
public class TrackingNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id", referencedColumnName = "id") // Correctly references the 'id' of NotificationTemplate
    private NotificationTemplate templateId;
    @Enumerated(EnumType.STRING)
    private Template template;
    @Enumerated(EnumType.STRING)
    private Channel channel;
    @Enumerated(EnumType.STRING)
    private Status status;

    private String description;
}
