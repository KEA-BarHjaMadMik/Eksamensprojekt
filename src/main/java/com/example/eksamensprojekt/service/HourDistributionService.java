package com.example.eksamensprojekt.service;

import com.example.eksamensprojekt.model.Project;
import com.example.eksamensprojekt.model.Task;
import com.example.eksamensprojekt.utils.DateUtil;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

@Service
public class HourDistributionService {

    public Map<LocalDate, Double> getProjectHourDistribution(Project project) {
        Map<LocalDate, Double> map = new TreeMap<>();

        LocalDate current = project.getStartDate();
        LocalDate endDate = project.getEndDate();

        if (current != null && endDate != null) {
            while (!current.isAfter(endDate)) {
                map.put(current, 0.0);
                current = current.plusDays(1);
            }
        }

        distributeProjectHours(project, map);
        return map;
    }

    private void distributeProjectHours(Project project, Map<LocalDate, Double> map) {
        // Include hours from tasks
        if (project.getTasks() != null) {
            for (Task task : project.getTasks()) {
                distributeTaskHours(task, map);
            }
        }

        // Include hours from subprojects recursively
        if (project.getSubProjects() != null) {
            for (Project sub : project.getSubProjects()) {
                distributeProjectHours(sub, map);
            }
        }
    }

    public void distributeTaskHours(Task task, Map<LocalDate, Double> map) {
        if (task.getSubTasks() == null || task.getSubTasks().isEmpty()) {
            double estimatedHours = task.getEstimatedHours();
            long businessDays = DateUtil.businessDaysBetween(task.getStartDate(), task.getEndDate());
            double dailyHrs = businessDays == 0 ? 0 : estimatedHours / businessDays;

            LocalDate current = task.getStartDate();
            LocalDate endDate = task.getEndDate();

            if (current != null && endDate != null) {
                while (!current.isAfter(endDate)) {
                    DayOfWeek dow = current.getDayOfWeek();
                    if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                        map.merge(current, dailyHrs, Double::sum);
                    }
                    current = current.plusDays(1);
                }
            }
        } else {
            for (Task sub : task.getSubTasks()) {
                distributeTaskHours(sub, map);
            }
        }
    }
}
