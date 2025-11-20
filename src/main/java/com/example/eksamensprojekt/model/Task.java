package com.example.eksamensprojekt.model;

import java.time.LocalDate;
import java.util.List;

public class Task {
    private int taskId;
    private int parentTaskId;
    private int projectId;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String description;
    private double estimatedHours;
    private double actualHours;
    private String status;
    private List<Task> subTasks;

    public Task(){}

    public Task(int taskId,
                int parentTaskId,
                int projectId, String title,
                LocalDate startDate,
                LocalDate endDate,
                String description,
                double estimatedHours,
                double actualHours,
                String status,
                List<Task> subtasks){
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

    public int getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(int parentTaskId) {
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
        return estimatedHours;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public double getActualHours() {
        return actualHours;
    }

    public void setActualHours(double actualHours) {
        this.actualHours = actualHours;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Task> getSubTasks() {
        return subTasks;
    }

    public void setSubTasks(List<Task> subTasks) {
        this.subTasks = subTasks;
    }
}

