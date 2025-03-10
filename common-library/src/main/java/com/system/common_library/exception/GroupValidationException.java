package com.system.common_library.exception;

import jakarta.validation.ConstraintViolation;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class GroupValidationException extends Exception {

    private Set<ConstraintViolation<Object>> violations;
}
