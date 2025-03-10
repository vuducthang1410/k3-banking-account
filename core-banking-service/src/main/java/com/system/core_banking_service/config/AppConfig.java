package com.system.core_banking_service.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.util.Optional;

@Configuration
public class AppConfig {

    @Bean
    public AuditorAware<String> auditorAware() {

        return new AuditorAware<String>() {
            @Override
            public @NotNull Optional<String> getCurrentAuditor() {
                return Optional.empty();
            }
        };
    }

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        return filter;
    }
}
