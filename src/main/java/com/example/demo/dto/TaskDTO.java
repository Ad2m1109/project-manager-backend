package com.example.demo.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Long projectId;
    private String projectName;
    private Long sprintId;
    private String sprintName;
    private Long assigneeId;
    private String assigneeName;
    private Long reporterId;
    private String reporterName;
    private Instant createdAt;
}
