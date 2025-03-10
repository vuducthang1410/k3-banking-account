package org.demo.loanservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomRequestLoggingFilter extends AbstractRequestLoggingFilter {
    public String secureMessageLog(String message) {
        String regex = "authorization:\"Bearer\\s+[^\"]*\"";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        // Thay thế phần authorization bằng chuỗi trống
        return matcher.replaceAll("authorization:\"Bearer <removed>\"");
    }
    @Override
    protected void beforeRequest(@NotNull HttpServletRequest request, @NotNull String message) {

    }

    @Override
    protected void afterRequest(@NotNull HttpServletRequest request, @NotNull String message) {
        logger.info("DATA REQUEST:: "+secureMessageLog(message));
    }
}
