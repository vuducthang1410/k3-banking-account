package com.example.notification_service.specification;

import com.example.notification_service.domain.entity.NotificationTemplate;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

@Slf4j
public class NotificationTemplateSpecification {

    public static Specification<NotificationTemplate> search(String title, String content, String channel, String event) {
        return (root, query, criteriaBuilder) -> {
            Predicate predicate = criteriaBuilder.conjunction();

            if (StringUtils.hasText(title)) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get("title"), "%" + title + "%"));
            }

            if (StringUtils.hasText(content)) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.like(root.get("content"), "%" + content + "%"));
            }

            if (StringUtils.hasText(channel)) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("channel"), channel));
            }

            if (StringUtils.hasText(event)) {
                predicate = criteriaBuilder.and(predicate,
                        criteriaBuilder.equal(root.get("event"), event));
            }
//            log.info(String.valueOf(predicate));
            return predicate;
        };
    }
}
