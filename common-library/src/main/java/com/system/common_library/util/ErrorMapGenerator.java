package com.system.common_library.util;

import jakarta.validation.ConstraintViolation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ErrorMapGenerator implements Serializable {

    public static String violationToErrorMap(Set<ConstraintViolation<Object>> violations) {

        Map<String, Set<String>> errorMap = new HashMap<>();
        if (!violations.isEmpty()) {

            for (ConstraintViolation<?> violation : violations) {

                String fieldName = violation.getPropertyPath().toString();
                String errorMessage = violation.getMessage();
                errorMap.computeIfAbsent(fieldName, k -> new HashSet<>()).add(errorMessage);
            }
        }

        return "Error list: " + errorMap;
    }
}
