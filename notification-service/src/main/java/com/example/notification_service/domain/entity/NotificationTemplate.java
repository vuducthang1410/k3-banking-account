package com.example.notification_service.domain.entity;

import com.example.notification_service.domain.enumValue.Channel;
import com.example.notification_service.domain.enumValue.Template;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification_templates")
public class NotificationTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    private Template event;

    private String title;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    private Channel channel;

    private String description;

    @Column(name = "create_at")
    private LocalDateTime createdAt;


    @Column(name = "create_by")
    private String createdBy;

    @Column(name = "update_at")
    private LocalDateTime updatedAt;


    @Column(name = "update_by")
    private String updatedBy;

    @Column(columnDefinition = "LONGTEXT",name = "required_fields")
    private String requiredFields;
}
