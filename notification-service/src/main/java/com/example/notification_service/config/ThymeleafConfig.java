package com.example.notification_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templateresolver.StringTemplateResolver;

@Configuration
public class ThymeleafConfig {

    @Bean
    public TemplateEngine htmlTemplateEngine() {
        TemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(htmlTemplateResolver());
        return templateEngine;
    }

    @Bean
    public TemplateEngine textTemplateEngine() {
        TemplateEngine templateEngine = new SpringTemplateEngine();
        templateEngine.setTemplateResolver(textTemplateResolver());
        return templateEngine;
    }

    @Bean
    public StringTemplateResolver htmlTemplateResolver() {
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode("HTML"); // For handling HTML templates
        resolver.setCacheable(false);
        return resolver;
    }

    @Bean
    public StringTemplateResolver textTemplateResolver() {
        StringTemplateResolver resolver = new StringTemplateResolver();
        resolver.setTemplateMode("TEXT"); // Ensure it processes plain text
        resolver.setCacheable(false);
        return resolver;
    }
}


