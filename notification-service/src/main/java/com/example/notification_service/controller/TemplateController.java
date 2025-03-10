package com.example.notification_service.controller;

import com.example.notification_service.domain.dto.NotificationTemplateRequestDTO;
import com.example.notification_service.domain.dto.NotificationTemplateResponseDTO;
import com.example.notification_service.domain.entity.NotificationTemplate;
import com.example.notification_service.service.interfaces.NotificationTemplateService;
import com.example.notification_service.service.interfaces.ObjectConverter;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/template")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:8000") // Allow specific origin
public class TemplateController {
    private final NotificationTemplateService notificationTemplateService;
    private final ObjectConverter objectConverter;


    @GetMapping
    public ResponseEntity<Page<NotificationTemplateResponseDTO>> getTemplates(@Nullable Pageable pageable) {
        return  ResponseEntity.ok(notificationTemplateService.retrieveAllTemplatesDTO(pageable));
    }
    @GetMapping("/NoPagination")
    public ResponseEntity<List<NotificationTemplateResponseDTO>> getTemplatesWithoutPagination() {
        return  ResponseEntity.ok(notificationTemplateService.retrieveAllTemplatesDTOWithoutPagination());
    }
    @GetMapping("/template-list")
    public ResponseEntity<List<String>> getAllTemplateList(){
        return ResponseEntity.ok(notificationTemplateService.findDistinctTemplate());
    }
    @GetMapping("/{id}")
    public ResponseEntity<NotificationTemplate> getTemplateById(@PathVariable("id") Integer id) {
        Optional<NotificationTemplate> template = notificationTemplateService.retrieveTemplate(id);
        return template.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/searchByEvenChannel")
    public ResponseEntity<Page<NotificationTemplate>> getTemplateByTemplateNameAndEvent(
            @Nullable Pageable pageable,
            @RequestParam(required = false) String event,
            @RequestParam(required = false) String channel) {
        Page<NotificationTemplate> template = notificationTemplateService.retrieveTemplate(event, channel, pageable);
        return ResponseEntity.ok(template);
    }
    @GetMapping("/searchByEvenChannelWithoutPagination")
    public ResponseEntity<List<NotificationTemplate>> getTemplateByTemplateNameAndEventWithoutPagination(
            @RequestParam(required = false) String event,
            @RequestParam(required = false) String channel) {
        List<NotificationTemplate> template = notificationTemplateService.retrieveTemplateList(event, channel);
//        return template.map(ResponseEntity::ok)
//                .orElseGet(() -> ResponseEntity.badRequest().build());
        return ResponseEntity.ok(template);
    }
    @PutMapping("/updateTemplate/{id}")
    public ResponseEntity<?> updateTemplate(
            @PathVariable int id,
            @RequestBody NotificationTemplateRequestDTO updatedTemplate) {
          if (updatedTemplate == null) {
            return ResponseEntity.badRequest().body("Request body cannot be empty.");
        }

        // Check if at least one field is provided
        boolean hasValidField = (updatedTemplate.getTitle() != null && !updatedTemplate.getTitle().isEmpty()) ||
                (updatedTemplate.getDescription() != null && !updatedTemplate.getDescription().isEmpty()) ||
                (updatedTemplate.getContent() != null && !updatedTemplate.getContent().isEmpty());

        if (!hasValidField) {
            return ResponseEntity.badRequest().body("At least one field (title, description, content) must be provided.");
        }
        Optional<NotificationTemplate> existingTemplateOpt = notificationTemplateService.retrieveTemplate(id);
        if (existingTemplateOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        NotificationTemplate existingTemplate = existingTemplateOpt.get();
        String requiredParameters = existingTemplate.getRequiredFields();

        // Validate content parameters
        if (updatedTemplate.getContent() != null && !updatedTemplate.getContent().isEmpty()){
            if( !notificationTemplateService.checkTemplateCompriseAllNeededParameters(
                objectConverter.covertStringtoListStringParameters(requiredParameters), updatedTemplate.getContent())) {
                log.debug("Template do not comprise all needed parameters");
                return ResponseEntity.badRequest().body("Template do not comprise all needed parameters");
            }
            existingTemplate.setContent(updatedTemplate.getContent());
        }

        // ✅ Fix: Only update fields if they are not null or empty
        if (updatedTemplate.getTitle() != null && !updatedTemplate.getTitle().isEmpty()) {
            existingTemplate.setTitle(updatedTemplate.getTitle());
        }
        if (updatedTemplate.getDescription() != null && !updatedTemplate.getDescription().isEmpty()) {
            existingTemplate.setDescription(updatedTemplate.getDescription());
        }

        existingTemplate.setUpdatedAt(LocalDateTime.now());
        NotificationTemplate savedTemplate = notificationTemplateService.saveTemplate(existingTemplate);
        NotificationTemplateResponseDTO responseDTO = NotificationTemplateResponseDTO.builder()
                .title(savedTemplate.getTitle())
                .content(savedTemplate.getContent())
                .channel(savedTemplate.getChannel())
                .description(savedTemplate.getDescription())
                .event(savedTemplate.getEvent())
                .build();
        return ResponseEntity.ok(responseDTO);
    }
}