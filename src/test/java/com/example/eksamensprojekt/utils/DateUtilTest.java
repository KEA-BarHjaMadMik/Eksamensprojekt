package com.example.eksamensprojekt.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilTest {

    @Test
    void businessDaysShouldEqualTwo() {
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 2);

        assertEquals(2, DateUtil.businessDaysBetween(startDate,endDate));
    }

    @Test
    void businessDaysShouldEqualFive() {
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 7);

        assertEquals(5, DateUtil.businessDaysBetween(startDate,endDate));
    }

    @Test
    void businessDaysShouldEqualTen() {
        LocalDate startDate = LocalDate.of(2025, 12, 1);
        LocalDate endDate = LocalDate.of(2025, 12, 14);

        assertEquals(10, DateUtil.businessDaysBetween(startDate,endDate));
    }
}