package com.luxus.tinterest.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class InterestTests {

    @Test
    void unsavedInterestsWithDifferentNamesStayDistinct() {
        Interest running = Interest.builder().name("Running").build();
        Interest books = Interest.builder().name("Books").build();

        assertNotEquals(running, books);
        assertNotEquals(running.hashCode(), books.hashCode());
    }

    @Test
    void unsavedInterestsCompareByNameIgnoringCase() {
        Interest runningUpper = Interest.builder().name("Running").build();
        Interest runningLower = Interest.builder().name("running").build();

        assertEquals(runningUpper, runningLower);
        assertEquals(runningUpper.hashCode(), runningLower.hashCode());
    }

    @Test
    void savedInterestsCompareById() {
        Interest first = Interest.builder().id(7L).name("Books").build();
        Interest second = Interest.builder().id(7L).name("Music").build();

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }
}
