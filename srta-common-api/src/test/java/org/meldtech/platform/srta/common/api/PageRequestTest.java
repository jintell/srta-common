package org.meldtech.platform.srta.common.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PageRequestTest {

    @Test
    void of_validatesAndCreatesOneBasedPageRequest() {
        PageRequest request = PageRequest.of(1, 20);

        assertEquals(1, request.getPage());
        assertEquals(20, request.getSize());
    }

    @Test
    void of_pageBelowOneThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> PageRequest.of(0, 20));
    }

    @Test
    void of_nonPositiveSizeThrowsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () -> PageRequest.of(1, 0));
    }

    @Test
    void toZeroBasedPageIndex_convertsCorrectly() {
        PageRequest request = PageRequest.of(3, 10);

        assertEquals(2, request.toZeroBasedPageIndex());
    }
}