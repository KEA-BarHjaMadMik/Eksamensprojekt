package com.example.eksamensprojekt.model;

import com.example.eksamensprojekt.utils.DateUtil;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Project {
    private int projectId;
    private int ownerId;
    private Integer parentProjectId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Start date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private List<Project> subProjects;
    private List<Task> tasks;

    public Project() {
        this.subProjects = new ArrayList<>();
        this.tasks = new ArrayList<>();
    }

    public Project(int projectId,
                   int ownerId,
                   Integer parentProjectId,
                   String title,
                   String description,
                   LocalDate startDate,
                   LocalDate endDate,
                   List<Project> subProjects,
                   List<Task> tasks) {
        this.projectId = projectId;
        this.ownerId = ownerId;
        this.parentProjectId = parentProjectId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.subProjects = subProjects;
        this.tasks = tasks;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getParentProjectId() {
        return parentProjectId;
    }

    public void setParentProjectId(Integer parentProjectId) {
        this.parentProjectId = parentProjectId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<Project> getSubProjects() {
        return subProjects;
    }

    public void setSubProjects(List<Project> subProjects) {
        this.subProjects = subProjects;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
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
        return tasks.stream().mapToDouble(Task::getEstimatedHours).sum()
                + subProjects.stream().mapToDouble(Project::getEstimatedHours).sum();
    }

    public double getActualHours() {
        return tasks.stream().mapToDouble(Task::getActualHours).sum()
                + subProjects.stream().mapToDouble(Project::getActualHours).sum();
    }

    public long getDays() {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1; // add 1 to include start date
    }

    public long getBusinessDays() {
        return DateUtil.businessDaysBetween(startDate,endDate);
    }

    public double getAvgDailyEstimatedHours() {
        return getEstimatedHours() / getBusinessDays();
    }

    // Returns a map of LocalDate -> estimated hours for the entire project,
    // including all tasks and subprojects, excluding weekends.
    public Map<LocalDate, Double> getDistributedHours() {
        Map<LocalDate, Double> map = new TreeMap<>();
        distributeHours(map);
        return map;
    }

    // Private helper that recursively distributes estimated hours across
    // tasks and subprojects, summing into the provided map.
    private void distributeHours(Map<LocalDate, Double> map) {
        // Include hours from tasks
        for (Task task : tasks) {
            Map<LocalDate, Double> taskHours = task.getDistributedTaskHours();
            taskHours.forEach((date, hours) -> map.merge(date, hours, Double::sum));
        }

        // Include hours from subprojects recursively
        for (Project sub : subProjects) {
            sub.distributeHours(map);
        }
    }
}
