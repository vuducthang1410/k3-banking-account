package com.system.account_service.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateTimeUtils {
    private static final Logger log = LoggerFactory.getLogger(DateTimeUtils.class);

    public static final String DD_MM_YYYY_HH_MM = "dd/MM/yyyy HH:mm";
    public static final String HH_MM_DD_MM_YYYY = "HH:mm dd/MM/yyyy";
    public static final String YYYY_MM_DD = "yyyy/MM/dd";
    public static final String DD_MM_YYYY = "dd/MM/yyyy";
    public static final String E_DD_MM_YYYY = "E, dd/MMM/yyyy";
    public static final String DD_MM_YYYY_HH_MM_A = "dd/MM/yyyy hh:mm a";

    public static String format(String format, Date date) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        }
        catch (Exception ex){
            log.error("An error occurred while formatting the date {}", ex.getMessage());
            return "";
        }
    }

    public static String format(String format, LocalDateTime localDateTime) {
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return localDateTime.format(formatter);
        }
        catch (Exception ex){
            log.error("An error occurred while formatting the date {}", ex.getMessage());
            return "";
        }
    }

    public static String format(String format, ZonedDateTime zonedDateTime) {
        try{
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            return zonedDateTime.format(formatter);
        }
        catch (Exception ex){
            log.error("An error occurred while formatting the date {}", ex.getMessage());
            return "";
        }
    }

    public static LocalDate getCurrentDate() {
        return LocalDate.now();
    }

    public static int getDaysInMonth(int year, int month) {
        YearMonth yearMonthObj = YearMonth.of(year, month);
        return yearMonthObj.lengthOfMonth();
    }

    public static Date localDateTimeToDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }
}
