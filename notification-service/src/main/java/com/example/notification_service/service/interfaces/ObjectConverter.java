package com.example.notification_service.service.interfaces;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ObjectConverter {
    Map<String, Object> convertToMap(Object obj) ;
    String convertToJson(Object obj) ;
    List<String> covertStringtoListStringParameters(String listParam) ;
    String convertListParametersToString(List<String> list) ;
     String getDate(LocalDate date);
     String getDate(LocalDateTime datetime);
     String getDateTime(LocalDateTime datetime);
     String getMoneyFormat(BigDecimal money);
     String getPercentageFormat(Double rate);
}
