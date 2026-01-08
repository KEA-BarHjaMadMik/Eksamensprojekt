package com.example.eksamensprojekt.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void getEstimatedHours() {
        Task standaloneTask = new Task();
        standaloneTask.setEstimatedHours(7.5);

        assertEquals(7.5, standaloneTask.getEstimatedHours());

        Task sub1 = new Task();
        sub1.setEstimatedHours(2.0);

        Task sub2 = new Task();
        sub2.setEstimatedHours(3.25);

        Task parent = new Task();
        parent.setEstimatedHours(999.0); // should be ignored when subtasks exist
        parent.setSubTasks(List.of(sub1, sub2));

        assertEquals(5.25, parent.getEstimatedHours());
    }

    @Test
    void getActualHours() {
        Task standaloneTask = new Task();
        standaloneTask.setActualHours(4.0);

        assertEquals(4.0, standaloneTask.getActualHours());

        Task sub1 = new Task();
        sub1.setActualHours(1.5);

        Task sub2 = new Task();
        sub2.setActualHours(2.25);

        Task parent = new Task();
        parent.setActualHours(10.0); // included in sum when subtasks exist
        parent.setSubTasks(List.of(sub1, sub2));

        assertEquals(13.75, parent.getActualHours());
    }

    @Test
    void isSubtaskShouldBeFalseWhenNoParentTaskId() {
        Task task = new Task();
        task.setParentTaskId(null);

        assertFalse(task.isSubtask());
    }

    @Test
    void isSubtaskShouldBeTrueWhenParentTaskIdSet() {
        Task task = new Task();
        task.setParentTaskId(123);

        assertTrue(task.isSubtask());
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
}