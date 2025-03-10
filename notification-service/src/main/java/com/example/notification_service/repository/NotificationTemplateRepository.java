package com.example.notification_service.repository;

import com.example.notification_service.domain.entity.NotificationTemplate;
import com.example.notification_service.domain.enumValue.Channel;
import com.example.notification_service.domain.enumValue.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationTemplateRepository extends CrudRepository<NotificationTemplate, Integer> {
//    Page<NotificationTemplate> findByTitle(String templateName);
    Page<NotificationTemplate> findByEvent(Template event, Pageable pageable);
    Page<NotificationTemplate> findByChannel(Channel channel, Pageable pageable);
    Optional<NotificationTemplate> findById(int id);
    Page<NotificationTemplate> findByEventAndChannel(Template template, Channel channel, Pageable pageable);
    Optional<NotificationTemplate> findByEventAndChannel(Template template, Channel channel);
    Page<NotificationTemplate> findAll(Pageable pageale);
    @Query("SELECT DISTINCT n.event FROM NotificationTemplate n")
    List<String> findDistinctTemplate();
    Iterable<NotificationTemplate> findByEvent(Template event);
    Iterable<NotificationTemplate> findByChannel(Channel channel);
//    Page<NotificationTemplateResponseDTO> findByEventAndChannelAndTitleAndContent(String template, String channel, String title, String Content);

}