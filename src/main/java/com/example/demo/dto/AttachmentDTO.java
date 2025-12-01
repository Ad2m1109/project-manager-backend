package com.example.demo.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class AttachmentDTO {
    private Long id;
    private String fileName;
    private String fileUrl;
    private String fileType;
    private Long fileSize;
    private Long taskId;
    private String taskTitle;
    private Long uploadedById;
    private String uploadedByName;
    private Instant uploadedAt;
}
