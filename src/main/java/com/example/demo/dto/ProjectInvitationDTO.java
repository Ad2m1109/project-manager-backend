package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectInvitationDTO {
    private Long id;
    private Long projectId;
    private String projectName;
    private Long invitedUserId;
    private String invitedUserName;
    private Long invitedById;
    private String invitedByName;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
