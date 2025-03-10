package com.system.napas_service.service;

import com.system.napas_service.service.interfaces.PagingService;
import com.system.napas_service.util.Constant;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PagingServiceImpl implements PagingService {

    private final MessageSource messageSource;

    @Override
    public Pageable getPageable(String sort, int page, int limit, Class<?> type) {

        if (page < 0) throw new InvalidParameterException(
                messageSource.getMessage(Constant.INVALID_PAGE_NUMBER, null, LocaleContextHolder.getLocale()));
        if (limit < 1) throw new InvalidParameterException(
                messageSource.getMessage(Constant.INVALID_PAGE_SIZE, null, LocaleContextHolder.getLocale()));

        List<Sort.Order> order = new ArrayList<>();

        Set<String> sourceFieldList = this.getAllFields(type);
        String[] subSort = sort.split(",");
        if (this.checkPropertyPresent(sourceFieldList, subSort[0])) {

            order.add(new Sort.Order(this.getSortDirection(subSort[1]), subSort[0]));
        } else {

            throw new InvalidParameterException("{" + subSort[0] + "} " +
                    messageSource.getMessage(Constant.INVALID_PROPERTY, null, LocaleContextHolder.getLocale()));
        }

        return PageRequest.of(page, limit).withSort(Sort.by(order));
    }

    private Set<String> getAllFields(Class<?> type) {

        Set<String> fields = new HashSet<>();
        //loop the fields using Java Reflections
        for (Field field : type.getDeclaredFields()) {

            fields.add(field.getName());
        }
        //recursive call to getAllFields
        if (type.getSuperclass() != null) {

            fields.addAll(getAllFields(type.getSuperclass()));
        }

        return fields;
    }

    private Sort.Direction getSortDirection(String direction) {

        if (direction.equals("asc")) {

            return Sort.Direction.ASC;
        } else if (direction.equals("desc")) {

            return Sort.Direction.DESC;
        }

        return Sort.Direction.ASC;
    }

    private boolean checkPropertyPresent(Set<String> properties, String propertyName) {

        return properties.contains(propertyName);
    }
}
