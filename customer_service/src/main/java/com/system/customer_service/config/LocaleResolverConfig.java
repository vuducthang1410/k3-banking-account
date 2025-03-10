package com.system.customer_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

@Configuration
public class LocaleResolverConfig {

    // Tạo Bean để quản lý các tệp tin tin nhắn bằng i18n, đồng thời tải các tập tin
    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {

        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setCacheSeconds(3600); // Cache 1 tiếng

        return messageSource;
    }

    // Xác định locale(vùng miền) nào sẽ được sử dụng
    @Bean
    public LocaleResolver localeResolver() {
        // Xác định sử dụng Http header Accept-Language từ phía client để xác định ngôn ngữ
        AcceptHeaderLocaleResolver localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(Locale.ENGLISH); // Đặt mặc định cho miền US
        return localeResolver;
    }
}
