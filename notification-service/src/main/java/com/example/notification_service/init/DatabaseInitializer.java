package com.example.notification_service.init;

import com.example.notification_service.service.interfaces.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component("databaseInitBean")
@RequiredArgsConstructor
public class DatabaseInitializer implements CommandLineRunner {

    private final NotificationTemplateService notificationTemplateService;
    @Override
    public void run(String... args) throws Exception {
        notificationTemplateService.loadTemplatesIfNotExist();
    }
}
