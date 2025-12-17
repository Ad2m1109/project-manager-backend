package com.example.demo.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ProjectDashboardDTO {
    private double overallProgress;
    private SprintDTO activeSprint;
    private Map<String, Long> taskDistribution;
    private long blockedTasksCount;
    private long overdueTasksCount;
}
