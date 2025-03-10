package com.system.napas_service.service.interfaces;

import org.springframework.data.domain.Pageable;

public interface PagingService {

    Pageable getPageable(String sort, int page, int limit, Class<?> type);
}
