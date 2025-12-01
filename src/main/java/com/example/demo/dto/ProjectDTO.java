package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private String priority;
    private String status;
    private LocalDate startDate;
    private Long founderId;
    private String founderName;
}
