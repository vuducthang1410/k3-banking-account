package com.example.notification_service.service;

import com.example.notification_service.service.interfaces.ObjectConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ObjectConverterImpl implements ObjectConverter {
    private final ObjectMapper objectMapper;
    @Override
    public Map<String, Object> convertToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        Class<?> clazz = obj.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // Allow access to private fields
            try {
                map.put(field.getName(), field.get(obj));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Error accessing field: " + field.getName(), e);
            }
        }
        return map;
    }
    @Override
    public String convertToJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }
    @Override
    public List<String> covertStringtoListStringParameters(String listParam) {
        List<String> list = new ArrayList<>();
        if (listParam == null) {
            return list;
        }
        for (String param : listParam.split(",")) {
            list.add(param.trim());
        }
        return list;
    }
    @Override
    public String convertListParametersToString(List<String> list) {
        String result = "";
        for (String param : list) {
            result += param + ",";
        }
        return result.substring(0, result.length() - 1);
    }

    @Override
    public String getDate(LocalDate date) {
        if (date == null) {
            return "No Date set";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    @Override
    public String getDate(LocalDateTime datetime) {
        if (datetime == null) {
            return "No Date set";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return datetime.format(formatter);
    }

    @Override
    public String getDateTime(LocalDateTime datetime) {
        if (datetime == null) {
            return "No Date Time set";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
        return datetime.format(formatter);
    }

    @Override
    public String getMoneyFormat(BigDecimal money) {
        DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");
        String formattedAmount = decimalFormat.format(money);
        return formattedAmount + " VND";
    }

    @Override
    public String getPercentageFormat(Double value) {
        return String.format("%.1f%%", value * 100);
    }
}
