package com.example.eksamensprojekt.model;

public class ProjectRole {
    private String role;
    private String roleName;

    public ProjectRole(String role, String roleName) {
        this.role = role;
        this.roleName = roleName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return roleName;
    }
}
