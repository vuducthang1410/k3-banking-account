package com.example.notification_service.service;

import com.example.notification_service.domain.dto.NotificationTemplateResponseDTO;
import com.example.notification_service.domain.entity.NotificationTemplate;
import com.example.notification_service.domain.enumValue.Channel;
import com.example.notification_service.domain.enumValue.Template;
import com.example.notification_service.repository.NotificationTemplateRepository;
import com.example.notification_service.service.interfaces.NotificationTemplateService;
import com.example.notification_service.service.interfaces.ObjectConverter;
import com.example.notification_service.service.interfaces.SMSRegistationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationTemplateServiceImpl implements NotificationTemplateService {
    private final NotificationTemplateRepository notificationTemplateRepository;
    private final ObjectMapper objectMapper;
    private final SMSRegistationService smsRegistationService ;
    private final ObjectConverter objectConverter;

    @Override
    public void loadTemplatesIfNotExist() throws IOException {
        if(notificationTemplateRepository.count() == 0) {
            loadTemplatesFromJson();
            smsRegistationService.registSMSService("123456","0899332074");
        }
    }
    @Override
    public void loadTemplatesFromJson() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("notification_mapping.json");
        JsonNode rootNode = objectMapper.readTree(inputStream);
        for (Iterator<Map.Entry<String, JsonNode>> it = rootNode.fields(); it.hasNext(); ) {
            Map.Entry<String, JsonNode> entry = it.next();
            String type = entry.getKey(); // SMS or EMAIL
            JsonNode eventsNode = entry.getValue();


            // Iterate over notification events (TRANSACTION_SUCCESS, OTP_VERIFICATION, etc.)
            for (Iterator<Map.Entry<String, JsonNode>> eventIt = eventsNode.fields(); eventIt.hasNext(); ) {
                Map.Entry<String, JsonNode> eventEntry = eventIt.next();
                String event = eventEntry.getKey();
                JsonNode details = eventEntry.getValue();

                // Extract details from JSON
                String template = details.has("template") ? details.get("template").asText() : null;
                String content = readTemplate(type+"/"+template);
                String title = details.has("title") ? details.get("title").asText() : null;
                String description = details.has("description") ? details.get("description").asText() : null;
                String requiredParameters = details.has("required") ? details.get("required").asText() : null;
                boolean validTemplate = checkTemplateCompriseAllNeededParameters(objectConverter.covertStringtoListStringParameters(requiredParameters), content);
                if (!validTemplate) {
                    log.warn("Template {} is not valid", template);
                    continue;
                }
                // Save to database
                log.info("Template {} is valid", template);
                if(type.equalsIgnoreCase("email") ){
                    NotificationTemplate notificationTemplate = NotificationTemplate.builder()
                            .channel(Channel.EMAIL)
                            .event( Template.valueOf(event))
                            .content(content)
                            .title(title)
                            .description(description)
                            .createdAt(LocalDateTime.now())
                            .createdBy("System")
                            .requiredFields(requiredParameters)
                            .build();
                    notificationTemplateRepository.save(notificationTemplate);
                }
               else if(type.equalsIgnoreCase("sms")){
                    NotificationTemplate notificationTemplate = NotificationTemplate.builder()
                            .channel(Channel.SMS)
                            .event( Template.valueOf(event))
                            .content(content)
                            .title(title)
                            .description(description)
                            .requiredFields(requiredParameters)
                            .createdAt(LocalDateTime.now())
                            .createdBy("System")
                            .build();
                    notificationTemplateRepository.save(notificationTemplate);
                }
                else if(type.equalsIgnoreCase("system")){
                    NotificationTemplate notificationTemplate = NotificationTemplate.builder()
                            .channel(Channel.SYSTEM)
                            .event( Template.valueOf(event))
                            .content(content)
                            .title(title)
                            .description(description)
                            .createdAt(LocalDateTime.now())
                            .requiredFields(requiredParameters)
                            .createdBy("System")
                            .build();
                    notificationTemplateRepository.save(notificationTemplate);
                }
            }
        }
    }
    @Override
    public String readTemplate(String templatePath) throws IOException {

        try {
            ClassPathResource resource = new ClassPathResource("templates/"+templatePath);
            return Files.readString(resource.getFile().toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Cannot read template {}", templatePath);
            log.warn(e.getMessage());
            throw new RuntimeException("Error reading file: " + templatePath, e);
        }
    }
    @Override
    public boolean checkTemplateCompriseAllNeededParameters(List<String> parameters, String template){
        log.info("Checking template with parameters {}", parameters);
        for(String parameter : parameters){
            if(!template.contains(parameter)){
                log.info("Parameter is not in template {}", parameter);
                return false;
            }
            log.debug("Parameter detached {}", parameter);
        }
        return true;
    }
    @Override
    public Optional<NotificationTemplate> retrieveTemplate(Template template, Channel channel) {
        return notificationTemplateRepository.findByEventAndChannel(template, channel);
    }

    @Override
    public List<NotificationTemplate> retrieveTemplateList(String template, String channel) {
        // Convert Strings to Enum
        Template templateEnum;
        Channel channelEnum;
        if(channel == null && template == null){
            Iterable<NotificationTemplate> list = notificationTemplateRepository.findAll();
            return StreamSupport.stream(list.spliterator(),false).collect(Collectors.toList());
        }

        else if(template == null){
            channelEnum = Channel.valueOf(channel.toUpperCase());
            log.info(channelEnum.name());
            Iterable<NotificationTemplate> list = notificationTemplateRepository.findByChannel(channelEnum);
            return StreamSupport.stream(list.spliterator(),false).collect(Collectors.toList());
        }
        else if(channel == null){
            templateEnum = Template.valueOf(template.toUpperCase());
            log.info(templateEnum.name());
            Iterable<NotificationTemplate> list = notificationTemplateRepository.findByEvent(templateEnum);
            return StreamSupport.stream(list.spliterator(),false).collect(Collectors.toList());

        }
        templateEnum = Template.valueOf(template.toUpperCase());
        log.info(templateEnum.name());
        channelEnum = Channel.valueOf(channel.toUpperCase());
        log.info(channelEnum.name());
        Optional<NotificationTemplate> notificationTemplateOptional = notificationTemplateRepository.findByEventAndChannel(templateEnum, channelEnum);
        return notificationTemplateOptional
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public Page<NotificationTemplate> retrieveTemplate(String template, String channel, Pageable pageable) {
        Template templateEnum;
        Channel channelEnum;
        if(channel == null && template == null){
            return notificationTemplateRepository.findAll(pageable);
        }

        else if(template == null){
            channelEnum = Channel.valueOf(channel.toUpperCase());
            log.info(channelEnum.name());
            return notificationTemplateRepository.findByChannel(channelEnum,pageable);
        }
        else if(channel == null){
            templateEnum = Template.valueOf(template.toUpperCase());
            log.info(templateEnum.name());
            return notificationTemplateRepository.findByEvent(templateEnum,pageable);
        }
        templateEnum = Template.valueOf(template.toUpperCase());
        log.info(templateEnum.name());
        channelEnum = Channel.valueOf(channel.toUpperCase());
        log.info(channelEnum.name());
        return notificationTemplateRepository.findByEventAndChannel(templateEnum, channelEnum, pageable);
    }

    @Override
    public Optional<NotificationTemplate> retrieveTemplate(int id) {
        return notificationTemplateRepository.findById(id);
    }

    @Override
    public Iterable<NotificationTemplate> retrieveAllTemplates() {
        return notificationTemplateRepository.findAll();
    }

    @Override
    public Page<NotificationTemplateResponseDTO> retrieveAllTemplatesDTO(Pageable pageable) {
        Page<NotificationTemplate> list =  notificationTemplateRepository.findAll(pageable);

        return list.map(this::convertToDTO);
    }

    @Override
    public List<String> findDistinctTemplate() {
        return notificationTemplateRepository.findDistinctTemplate();
    }

    @Override
    public List<NotificationTemplateResponseDTO> retrieveAllTemplatesDTOWithoutPagination() {
        Iterable<NotificationTemplate> list = notificationTemplateRepository.findAll();
        return StreamSupport.stream(list.spliterator(),false).map(this::convertToDTO).collect(Collectors.toList());
    }

    @Override
    public Page<NotificationTemplateResponseDTO> retrieveTemplate(Pageable pageable, String event, String title, String content, String channel) {
        Page<NotificationTemplate> notificationTemplates;
        if(event == null && title == null && content == null && channel == null){
            return retrieveAllTemplatesDTO(pageable);
        }
//        if(event != null && title != null && content != null && channel != null){
////            return notificationTemplateRepository.findByEventAndChannelAndTitleAndContent();
//        }
        return retrieveAllTemplatesDTO(pageable);

    }

    @Override
    public Page<NotificationTemplateResponseDTO> searchTemplatesSpecification(String title, String content, String channel, String event, Pageable pageable) {

//        Page<NotificationTemplate> entities=  notificationTemplateRepository.findAll(NotificationTemplateSpecification.search(title, content, channel, event), pageable);
        Page<NotificationTemplate> entities=  notificationTemplateRepository.searchTemplates(title, content, channel, event, pageable);

        return entities.map(this::convertToDTO);
    }

    private NotificationTemplateResponseDTO convertToDTO(NotificationTemplate notificationTemplate){
         return NotificationTemplateResponseDTO.builder()
                .id(notificationTemplate.getId())
                .event(notificationTemplate.getEvent())
                .channel(notificationTemplate.getChannel())
                .title(notificationTemplate.getTitle())
                .description(notificationTemplate.getDescription())
                .content(notificationTemplate.getContent())
                .build();
    }
    @Override
    public NotificationTemplate saveTemplate(NotificationTemplate template) {
        return notificationTemplateRepository.save(template);
    }
}
