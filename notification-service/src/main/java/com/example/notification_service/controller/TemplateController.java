package com.example.notification_service.controller;

import com.example.notification_service.domain.dto.NotificationTemplateRequestDTO;
import com.example.notification_service.domain.dto.NotificationTemplateResponseDTO;
import com.example.notification_service.domain.entity.NotificationTemplate;
import com.example.notification_service.domain.enumValue.Channel;
import com.example.notification_service.service.interfaces.NotificationTemplateService;
import com.example.notification_service.service.interfaces.ObjectConverter;
import io.micrometer.common.lang.Nullable;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/v1/template")
@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:8000") // Allow specific origin
public class TemplateController {
    private final NotificationTemplateService notificationTemplateService;
    private final ObjectConverter objectConverter;


    @GetMapping("")
    @Operation(summary = "Get template list with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content =
                    {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
    public ResponseEntity<Page<NotificationTemplateResponseDTO>> getTemplates(@Nullable Pageable pageable) {
        return  ResponseEntity.ok(notificationTemplateService.retrieveAllTemplatesDTO(pageable));
    }
    @Operation(summary = "Get template list without pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success", content =
                    {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "400", description = "Bad Request", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "404", description = "Not Found", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
            @ApiResponse(responseCode = "500", description = "Internal Server Error", content =
                    {@Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))}),
    })
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
    @GetMapping("/search")
    public Page<NotificationTemplateResponseDTO> searchTemplates(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) String channel,
            @RequestParam(required = false) String event,
            Pageable pageable) {
        return notificationTemplateService.searchTemplatesSpecification(title, content, channel, event, pageable);
    }
    @PutMapping("/updateTemplate/{id}")
    public ResponseEntity<?> updateTemplate(
            @PathVariable int id,
            @RequestBody NotificationTemplateRequestDTO updatedTemplate) {
          if (updatedTemplate == null) {
            return ResponseEntity.badRequest().body("Request body cannot be empty.");
        }

        // Check if at least one field is provided
//        boolean hasValidField = (updatedTemplate.getTitle() != null && !updatedTemplate.getTitle().isEmpty()) ||
//                (updatedTemplate.getDescription() != null && !updatedTemplate.getDescription().isEmpty()) ||
//                (updatedTemplate.getContent() != null && !updatedTemplate.getContent().isEmpty());
//
//        if (!hasValidField) {
//            return ResponseEntity.badRequest().body("At least one field (title, description, content) must be provided.");
//        }
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
                .id(savedTemplate.getId())
                .title(savedTemplate.getTitle())
                .content(savedTemplate.getContent())
                .channel(savedTemplate.getChannel())
                .description(savedTemplate.getDescription())
                .event(savedTemplate.getEvent())
                .build();
        return ResponseEntity.ok(responseDTO);
    }
    @PostMapping("/validate/{id}")
    public ResponseEntity<?> validateTemplate(
            @PathVariable int id,
            @RequestBody NotificationTemplateRequestDTO updatedTemplate) {
        log.info("Validating template {}", updatedTemplate);
        if (updatedTemplate == null) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "errors", List.of(Map.of("field", "general", "message", "Request body cannot be empty."))));
        }
        List<Map<String, String>> errors = new ArrayList<>();
        // Validate fields
        Optional<NotificationTemplate> existingTemplateOpt = notificationTemplateService.retrieveTemplate(id);
        if (existingTemplateOpt.isPresent()) {
            if(existingTemplateOpt.get().getChannel().equals(Channel.EMAIL)){
                if (updatedTemplate.getTitle() == null || updatedTemplate.getTitle().isEmpty()) {
                    errors.add(Map.of("field", "title", "message", "Title is required."));
                }
            }
        }
        else{
            errors.add(Map.of("field", "id", "message", "Template id does not exist."));
            return ResponseEntity.badRequest().body(Map.of("valid", false, "errors", errors));
        }
        if (updatedTemplate.getDescription() == null || updatedTemplate.getDescription().isEmpty()) {
            errors.add(Map.of("field", "description", "message", "Description is required."));
        }
        if (updatedTemplate.getContent() == null || updatedTemplate.getContent().isEmpty()) {
            errors.add(Map.of("field", "content", "message", "Content is required."));
        } else {
            String requiredParameters = existingTemplateOpt.get().getRequiredFields();
            boolean validContent = notificationTemplateService.checkTemplateCompriseAllNeededParameters(
                    objectConverter.covertStringtoListStringParameters(requiredParameters), updatedTemplate.getContent());

            if (!validContent) {
                errors.add(Map.of("field", "content", "message", "Template content does not include all required parameters."));
            }
        }
        if (!errors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "errors", errors));
        }
        return ResponseEntity.ok(Map.of("valid", true));
    }
}