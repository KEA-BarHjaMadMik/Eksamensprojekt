package com.example.eksamensprojekt.model;

import java.time.LocalDate;
import java.util.List;

public class Project {
    private int projectId;
    private int ownerID;
    private Integer parentProjectId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Project> subProjects;
    private List<Task> tasks;

    public Project() {
    }

    public Project(int projectId,
                   int ownerID,
                   int parentProjectId,
                   String title,
                   String description,
                   LocalDate startDate,
                   LocalDate endDate,
                   List<Project> subProjects,
                   List<Task> tasks) {
        this.projectId = projectId;
        this.ownerID = ownerID;
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

    public int getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(int ownerID) {
        this.ownerID = ownerID;
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

}
