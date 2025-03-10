package com.system.napas_service.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.jsonwebtoken.lang.Assert;
import org.springframework.data.domain.Page;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class PagedDTO<T> implements Serializable {

    private final Page<T> page;

    public PagedDTO(Page<T> page) {

        Assert.notNull(page, "Page must not be null");

        this.page = page;
    }

    @JsonProperty
    public List<T> getContent() {
        
        return page.getContent();
    }

    @Nullable
    @JsonProperty("page")
    public PageMetadata getMetadata() {

        return new PageMetadata(page.getSize(), page.getNumber(), page.getTotalElements(),
                page.getTotalPages());
    }

    @Override
    public boolean equals(@Nullable Object obj) {

        if (this == obj) {

            return true;
        }

        if (!(obj instanceof PagedDTO<?> that)) {

            return false;
        }

        return Objects.equals(this.page, that.page);
    }

    @Override
    public int hashCode() {

        return Objects.hash(page);
    }

    public record PageMetadata(long size, long number, long totalElements, long totalPages) {

        public PageMetadata {
            Assert.isTrue(size > -1, "Size must not be negative!");
            Assert.isTrue(number > -1, "Number must not be negative!");
            Assert.isTrue(totalElements > -1, "Total elements must not be negative!");
            Assert.isTrue(totalPages > -1, "Total pages must not be negative!");
        }
    }
}
