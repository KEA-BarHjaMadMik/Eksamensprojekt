package com.example.eksamensprojekt.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void getEstimatedHours() {
    }

    @Test
    void getActualHours() {
    }

    @Test
    void daysShouldEqualTwo() {
        Task task = new Task();
        task.setStartDate(LocalDate.of(2025, 12, 1));
        task.setEndDate(LocalDate.of(2025, 12, 2));

        assertEquals(2, task.getDays());
    }

    @Test
    void businessDaysShouldEqualTwo() {
        Task task = new Task();
        task.setStartDate(LocalDate.of(2025, 12, 1));
        task.setEndDate(LocalDate.of(2025, 12, 2));

        assertEquals(2, task.getBusinessDays());
    }
    @Test
    void businessDaysShouldEqualFive() {
        Task task = new Task();
        task.setStartDate(LocalDate.of(2025, 12, 1));
        task.setEndDate(LocalDate.of(2025, 12, 7));

        assertEquals(5, task.getBusinessDays());
    }

    @Test
    void businessDaysShouldEqualTen() {
        Task task = new Task();
        task.setStartDate(LocalDate.of(2025, 12, 1));
        task.setEndDate(LocalDate.of(2025, 12, 14));

        assertEquals(10, task.getBusinessDays());
    }

    @Test
    void dailyEstimatedHrsShouldBeTwoAndAHalf() {
        Task task = new Task();
        task.setEstimatedHours(5);
        task.setStartDate(LocalDate.of(2025, 12, 1));
        task.setEndDate(LocalDate.of(2025, 12, 2));

        assertEquals(2.5, task.getAvgEstimatedHoursPerBusinessDay());
    }
}