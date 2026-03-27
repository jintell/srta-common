package org.meldtech.platform.srta.common.api;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Standard envelope for paginated data.
 *
 * @param <T> The type of the items in the list.
 */
@Value
@Builder
public class PagedResponse<T> {
    List<T> content;
    int page;
    int size;
    long totalElements;
    int totalPages;
    boolean last;

    /**
     * Create a paged response from content and pagination details.
     */
    public static <T> PagedResponse<T> of(List<T> content, int page, int size, long totalElements) {
        if (page < 0) throw new IllegalArgumentException("Page index must be >= 0, got: " + page);
        if (size <= 0) throw new IllegalArgumentException("Page size must be > 0, got: " + size);
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return PagedResponse.<T>builder()
                .content(content != null ? content : List.of())
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .last(totalPages == 0 || page >= totalPages - 1)
                .build();
    }
}
