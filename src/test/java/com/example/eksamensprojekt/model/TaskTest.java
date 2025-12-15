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

    @Test
    void dailyEstimatedHrsShouldBeTwoAndAHalf() {
        Task task = new Task();
        task.setEstimatedHours(5);
        task.setStartDate(LocalDate.of(2025, 12, 1));
        task.setEndDate(LocalDate.of(2025, 12, 2));

        assertEquals(2.5, task.getAvgEstimatedHoursPerBusinessDay());
    }

    @Test
    void avgEstimatedHoursPerBusinessDayShouldBeZeroWhenNoBusinessDays() {
        // Saturday -> Sunday: 0 business days, so avg must be 0 (and no division-by-zero)
        Task task = new Task();
        task.setEstimatedHours(10);
        task.setStartDate(LocalDate.of(2025, 12, 6)); // Saturday
        task.setEndDate(LocalDate.of(2025, 12, 7));   // Sunday

        assertEquals(0.0, task.getAvgEstimatedHoursPerBusinessDay());
    }

    @Test
    void distributedTaskHoursShouldExcludeWeekendsAndDistributeEvenly() {
        // Mon..Fri => 5 business days, 10 hours => 2 hours/day
        Task task = new Task();
        task.setEstimatedHours(10.0);
        task.setStartDate(LocalDate.of(2025, 12, 1)); // Monday
        task.setEndDate(LocalDate.of(2025, 12, 7));   // Sunday

        Map<LocalDate, Double> map = task.getDistributedTaskHours();

        assertEquals(5, map.size());
        assertEquals(2.0, map.get(LocalDate.of(2025, 12, 1)));
        assertEquals(2.0, map.get(LocalDate.of(2025, 12, 2)));
        assertEquals(2.0, map.get(LocalDate.of(2025, 12, 3)));
        assertEquals(2.0, map.get(LocalDate.of(2025, 12, 4)));
        assertEquals(2.0, map.get(LocalDate.of(2025, 12, 5)));
    }

    @Test
    void distributedTaskHoursShouldMergeSubtasksByDate() {
        // Two subtasks overlapping Mon..Tue:
        // sub1: 4 hours over 2 business days => 2/day
        // sub2: 2 hours over 2 business days => 1/day
        // expected merged: 3/day on both dates
        Task sub1 = new Task();
        sub1.setEstimatedHours(4.0);
        sub1.setStartDate(LocalDate.of(2025, 12, 1)); // Monday
        sub1.setEndDate(LocalDate.of(2025, 12, 2));   // Tuesday

        Task sub2 = new Task();
        sub2.setEstimatedHours(2.0);
        sub2.setStartDate(LocalDate.of(2025, 12, 1)); // Monday
        sub2.setEndDate(LocalDate.of(2025, 12, 2));   // Tuesday

        Task parent = new Task();
        parent.setEstimatedHours(999.0); // ignored for distribution because parent has subtasks
        parent.setSubTasks(List.of(sub1, sub2));

        Map<LocalDate, Double> map = parent.getDistributedTaskHours();

        assertEquals(2, map.size());
        assertEquals(3.0, map.get(LocalDate.of(2025, 12, 1)));
        assertEquals(3.0, map.get(LocalDate.of(2025, 12, 2)));
    }
}