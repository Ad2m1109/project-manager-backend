package com.example.demo.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class TaskActivityDTO {
    private Long id;
    private Long taskId;
    private Long userId;
    private String userName;
    private String action;
    private String oldValue;
    private String newValue;
    private Instant createdAt;
}
