package com.example.eksamensprojekt.model;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Task {
    private int taskId;
    private Integer parentTaskId;
    private int projectId;
    private String title;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String description;
    private double estimatedHours;
    private double actualHours;
    private TaskStatus status;
    private List<Task> subTasks;

    public Task() {
    }

    public Task(int taskId,
                Integer parentTaskId,
                int projectId,
                String title,
                LocalDate startDate,
                LocalDate endDate,
                String description,
                double estimatedHours,
                double actualHours,
                TaskStatus status,
                List<Task> subtasks) {
        this.taskId = taskId;
        this.parentTaskId = parentTaskId;
        this.projectId = projectId;
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.estimatedHours = estimatedHours;
        this.actualHours = actualHours;
        this.status = status;
        this.subTasks = subtasks;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public Integer getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(Integer parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getEstimatedHours() {
        if (subTasks == null || subTasks.isEmpty()) {
            return estimatedHours;
        } else {
            return subTasks.stream().mapToDouble(Task::getEstimatedHours).sum();
        }
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public double getActualHours() {
        if (subTasks == null || subTasks.isEmpty()) {
            return actualHours;
        } else {
            // actual hours are based on time entries. Therefore, actual hours of parent task must be included in sum.
            return actualHours + subTasks.stream().mapToDouble(Task::getActualHours).sum();
        }
    }

    public void setActualHours(double actualHours) {
        this.actualHours = actualHours;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public List<Task> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<Task> subTasks) {
        this.subTasks = subTasks;
    }

    public boolean isSubtask() {
        return parentTaskId != null;
    }

    public long getDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1; // add 1 to include start date
    }

    public long getBusinessDays() {
        long days = 0;
        LocalDate current = startDate;

        // inclusive
        while (!current.isAfter(endDate)) {
            DayOfWeek dow = current.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                days++;
            }
            current = current.plusDays(1);
        }

        return days;
    }

    public double getAvgEstimatedHoursPerBusinessDay() {
        long businessDays = getBusinessDays();
        return businessDays == 0 ? 0 : getEstimatedHours() / businessDays;
    }

    // Returns a map of LocalDate -> estimated hours for the task,
    // including all subtasks, excluding weekends.
    public Map<LocalDate, Double> getDistributedTaskHours () {
        Map<LocalDate, Double> map = new TreeMap<>();
        distributeTaskHours(map);
        return map;
    }

    // Private helper that recursively distributes estimated hours across
    // tasks and subtasks, summing into the provided map.
    private void distributeTaskHours (Map<LocalDate, Double> map) {

        if(subTasks == null || subTasks.isEmpty()) {
            double dailyHrs = getAvgEstimatedHoursPerBusinessDay();

            LocalDate current = startDate;

            while (!current.isAfter(endDate)) {
                DayOfWeek dow = current.getDayOfWeek();
                if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                    map.merge(current, dailyHrs, Double::sum);
                }
                current = current.plusDays(1);
            }
        } else {
            for (Task sub : subTasks) {
                sub.distributeTaskHours(map);
            }
        }
    }
}

