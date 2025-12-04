package com.example.eksamensprojekt.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class TimeEntry {
    private int timeEntryId;

    @Positive
    private int taskId;

    @Positive
    private int userId;

    private String userEmail;

    @Positive(message = "Registrerede timer skal være mere end 0.")
    private double hoursWorked;

    @NotBlank(message = "Beskrivelse kan ikke være tom.")
    @Size(max = 150, message = "Beskrivelse kan ikke være mere end 150 tegn.")
    private String description;

    public TimeEntry(){}

    public TimeEntry(int timeEntryId, int taskId, int userId, String userEmail, double hoursWorked, String description) {
        this.timeEntryId = timeEntryId;
        this.taskId = taskId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.hoursWorked = hoursWorked;
        this.description = description;
    }

    public int getTimeEntryId() {
        return timeEntryId;
    }

    public void setTimeEntryId(int timeEntryId) {
        this.timeEntryId = timeEntryId;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
