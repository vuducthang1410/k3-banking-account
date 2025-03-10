package org.demo.loanservice.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RequestLoggingFilterConfig {
    public CustomRequestLoggingFilter customRequestLoggingFilter() {
        CustomRequestLoggingFilter customRequestLoggingFilter = new CustomRequestLoggingFilter();
        customRequestLoggingFilter.setIncludeQueryString(true);
        customRequestLoggingFilter.setIncludePayload(true);
        customRequestLoggingFilter.setIncludeClientInfo(true);
        customRequestLoggingFilter.setIncludeHeaders(true);
        customRequestLoggingFilter.setMaxPayloadLength(2003);
        return customRequestLoggingFilter;
    }
    @Bean
    public FilterRegistrationBean<CustomRequestLoggingFilter> requestLoggingFilter() {
        FilterRegistrationBean<CustomRequestLoggingFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(customRequestLoggingFilter());
        filterRegistrationBean.setOrder(1);
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }
}

