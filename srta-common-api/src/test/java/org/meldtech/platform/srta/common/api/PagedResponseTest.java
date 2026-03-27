package org.meldtech.platform.srta.common.api;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PagedResponseTest {

    @Test
    void of_calculatesTotalPagesAndLast() {
        PagedResponse<String> page = PagedResponse.of(List.of("a", "b"), 0, 2, 5);

        assertEquals(3, page.getTotalPages());
        assertFalse(page.isLast());
        assertEquals(2, page.getContent().size());
    }

    @Test
    void of_lastPageIsTrue() {
        PagedResponse<String> page = PagedResponse.of(List.of("e"), 2, 2, 5);

        assertTrue(page.isLast());
    }

    @Test
    void of_zeroSizeThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> PagedResponse.of(List.of(), 0, 0, 0));
    }

    @Test
    void of_negativePageThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> PagedResponse.of(List.of(), -1, 10, 0));
    }

    @Test
    void of_negativeSizeThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class,
                () -> PagedResponse.of(List.of(), 0, -1, 0));
    }

    @Test
    void of_nullContentDefaultsToEmptyList() {
        PagedResponse<String> page = PagedResponse.of(null, 0, 10, 0);

        assertNotNull(page.getContent());
        assertTrue(page.getContent().isEmpty());
        assertTrue(page.isLast());
    }

    @Test
    void of_zeroTotalElementsIsLastPage() {
        PagedResponse<String> page = PagedResponse.of(List.of(), 0, 10, 0);

        assertEquals(0, page.getTotalPages());
        assertTrue(page.isLast());
    }
}
