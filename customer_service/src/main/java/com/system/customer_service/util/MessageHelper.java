package com.system.customer_service.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
@Slf4j
public class MessageHelper {
    private static MessageSource messageSource;

    @Autowired
    public MessageHelper(MessageSource messageSource) {
        MessageHelper.messageSource = messageSource;
    }

    public static String getMessage(String key, Object[] args,Locale locale) {
        log.info("Locale {}", locale);
        return messageSource.getMessage(key, args, locale);
    }
}
