package com.system.account_service.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.WebRequest;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LocaleUtils {
    private final MessageSource messageSource;

    public String getLocaleMsg(String msgKey, WebRequest req, Object... args) {
        Locale locale = (req != null) ? req.getLocale() : Locale.ENGLISH;
        return messageSource.getMessage(msgKey, args, locale);
    }
}
