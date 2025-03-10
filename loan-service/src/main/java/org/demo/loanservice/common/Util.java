package org.demo.loanservice.common;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.demo.loanservice.controllers.exception.ServerErrorException;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

@RequiredArgsConstructor
@Component
public class Util {
    public static final String API_RESOURCE = "/api/v1";
    private final MessageSource messageSource;
    private final Logger logger = LogManager.getLogger(Util.class);
    private static final String CURRENCY_VN = "VND";

    /**
     * @param key the message key
     * @return the localized message
     */
    public String getMessageFromMessageSource(String key) {
        try {
            return messageSource.getMessage(key, null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            logger.error("Failed to retrieve message for key: {}", key, e);
            throw new ServerErrorException();
        }
    }

    /**
     * @param key         the message key
     * @param loanProduct the loan product name
     * @param namePeriod  the loan period name
     * @param amount      the loan amount
     */
    public String getMessageTransactionFromMessageSource(String key, String loanProduct, String namePeriod, String amount) {
        try {
            return messageSource.getMessage(key, new Object[]{loanProduct, namePeriod, amount}, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            logger.error("Failed to retrieve transaction message for key: {}, loanProduct: {}, namePeriod: {}, amount: {}",
                    key, loanProduct, namePeriod, amount, e);
            throw new ServerErrorException();
        }
    }

    /**
     * @param key        the message key
     * @param paramValue the parameter value for the message
     */
    public String getMessageTransactionFromMessageSource(String key, String paramValue) {
        try {
            return messageSource.getMessage(key, new Object[]{paramValue}, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            logger.error("Failed to retrieve transaction message for key: {}, paramValue: {}", key, paramValue, e);
            throw new ServerErrorException();
        }
    }

    public static String formatToVND(BigDecimal amount) {
        if (amount == null) {
            return "0".concat(" ").concat(CURRENCY_VN);
        }

        // Định dạng tiền tệ theo chuẩn Việt Nam
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');

        DecimalFormat decimalFormat = new DecimalFormat("#,###".concat(" ").concat(CURRENCY_VN), symbols);

        return decimalFormat.format(amount);
    }
}
