package com.example.notification_service.repository;

import com.example.notification_service.domain.entity.TrackingNotification;
import com.example.notification_service.domain.enumValue.Channel;
import com.example.notification_service.domain.enumValue.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface TrackingNotificationRepository extends CrudRepository<TrackingNotification, Integer> {
    Page<TrackingNotification> findAll(Pageable pageable);
    long countByStatus(Status status);

    long countByChannel(Channel channel);
}
