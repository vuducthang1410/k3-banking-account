package com.example.notification_service.service.interfaces;

import com.example.notification_service.domain.dto.NotificationTemplateResponseDTO;
import com.example.notification_service.domain.entity.NotificationTemplate;
import com.example.notification_service.domain.enumValue.Channel;
import com.example.notification_service.domain.enumValue.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


public interface NotificationTemplateService {
    void loadTemplatesIfNotExist() throws IOException;
    void loadTemplatesFromJson() throws IOException ;
    String readTemplate(String templatePath) throws IOException ;
    boolean checkTemplateCompriseAllNeededParameters(List<String> parameters, String template);
    Optional<NotificationTemplate> retrieveTemplate(Template template, Channel channel) ;
    List<NotificationTemplate> retrieveTemplateList(String template, String channel) ;
    Page<NotificationTemplate> retrieveTemplate(String template, String channel,Pageable pageable) ;
    Optional<NotificationTemplate> retrieveTemplate(int id);
    Iterable<NotificationTemplate> retrieveAllTemplates();
    NotificationTemplate saveTemplate(NotificationTemplate template);
    Page<NotificationTemplateResponseDTO> retrieveAllTemplatesDTO(Pageable pageable);
    List<String> findDistinctTemplate();
    List<NotificationTemplateResponseDTO> retrieveAllTemplatesDTOWithoutPagination();
    Page<NotificationTemplateResponseDTO> retrieveTemplate(Pageable pageable,  String event, String title, String content, String channel);
    Page<NotificationTemplateResponseDTO> searchTemplatesSpecification(String title, String content, String channel, String event, Pageable pageable) ;
}
