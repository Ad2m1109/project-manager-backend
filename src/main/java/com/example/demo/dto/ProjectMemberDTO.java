package com.example.demo.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ProjectMemberDTO {
    private Long userId;
    private String userName;
    private String userEmail;
    private Long projectId;
    private String projectName;
    private String roleInProject;
    private Instant joinedAt;
}
