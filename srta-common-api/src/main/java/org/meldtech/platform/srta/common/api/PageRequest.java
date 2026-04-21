package org.meldtech.platform.srta.common.api;

import lombok.Builder;
import lombok.Value;

/**
 * Shared 1-based page request contract.
 */
@Value
@Builder
public class PageRequest {
    int page;
    int size;

    public static PageRequest of(int page, int size) {
        if (page < 1) {
            throw new IllegalArgumentException("Page must be >= 1, got: " + page);
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Page size must be > 0, got: " + size);
        }

        return PageRequest.builder()
                .page(page)
                .size(size)
                .build();
    }

    public int toZeroBasedPageIndex() {
        return page - 1;
    }
}