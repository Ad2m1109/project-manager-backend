package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SprintDTO {
    private Long id;
    private String name;
    private String goal;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Long projectId;
    private String projectName;
    private Integer taskCount;
    private Double progress;
    private java.util.List<TaskDTO> tasks;
}
