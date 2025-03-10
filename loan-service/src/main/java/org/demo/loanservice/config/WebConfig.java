package org.demo.loanservice.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedHeaders("*")
                .allowedMethods("*")
                .maxAge(3600);
    }

    @Bean
    public FilterRegistrationBean<CustomRequestLoggingFilter> loggingFilter() {
        FilterRegistrationBean<CustomRequestLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CustomRequestLoggingFilter());
        registrationBean.setOrder(1);
        registrationBean.addUrlPatterns("/auth/*");
        return registrationBean;
    }
}
