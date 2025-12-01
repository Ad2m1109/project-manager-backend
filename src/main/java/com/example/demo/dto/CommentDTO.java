package com.example.demo.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class CommentDTO {
    private Long id;
    private String content;
    private Long taskId;
    private String taskTitle;
    private Long userId;
    private String userName;
    private Instant createdAt;
}
