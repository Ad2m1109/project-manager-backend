package com.example.demo.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatLogRequest {
    private Long userId;
    private Long projectId;
    private String content;
    private LocalDateTime date; // Optional, can be generated on backend
}
