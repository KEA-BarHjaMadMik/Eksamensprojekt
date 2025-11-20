package com.example.eksamensprojekt.model;

import java.time.LocalDate;

public class Project {
    private int projectId;
    private int parentProjectId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;

    public Project(int projectId, String title, String description, LocalDate startDate, LocalDate endDate){
        this.projectId = projectId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Project(int projectId, int parentProjectId, String title, String description, LocalDate startDate, LocalDate endDate){
        this.projectId = projectId;
        this.parentProjectId = parentProjectId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public int getProjectId() {
        return projectId;
    }

    public int getParentProjectId(){
        return parentProjectId;
    }

    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public LocalDate getStartDate(){
        return startDate;
    }

    public void setStartDate(){
        
    }

    public LocalDate getEndDate(){
        return endDate;
    }

    public void setEndDate(LocalDate endDate){
        this.endDate = endDate
    }
}
