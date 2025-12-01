package com.example.demo.controller;

import com.example.demo.dto.AttachmentDTO;
import com.example.demo.model.AppUser;
import com.example.demo.model.Attachment;
import com.example.demo.model.Task;
import com.example.demo.service.AppUserService;
import com.example.demo.service.AttachmentService;
import com.example.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final TaskService taskService;
    private final AppUserService appUserService;

    // Get all attachments for a specific task
    @GetMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<List<AttachmentDTO>> getTaskAttachments(@PathVariable Long taskId) {
        List<Attachment> attachments = attachmentService.findByTaskId(taskId);
        List<AttachmentDTO> dtos = attachments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Upload/create a new attachment for a task
    @PostMapping("/tasks/{taskId}/attachments")
    public ResponseEntity<AttachmentDTO> createAttachment(
            @PathVariable Long taskId,
            @RequestBody AttachmentDTO attachmentDTO) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        AppUser currentUser = appUserService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = taskService.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Attachment attachment = new Attachment();
        attachment.setFileName(attachmentDTO.getFileName());
        attachment.setFileUrl(attachmentDTO.getFileUrl());
        attachment.setTask(task);
        attachment.setUser(currentUser);

        Attachment savedAttachment = attachmentService.save(attachment);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedAttachment));
    }

    // Delete an attachment
    @DeleteMapping("/attachments/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
        if (attachmentService.findById(id).isPresent()) {
            attachmentService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Helper method to convert Attachment entity to AttachmentDTO
    private AttachmentDTO convertToDTO(Attachment attachment) {
        AttachmentDTO dto = new AttachmentDTO();
        dto.setId(attachment.getId());
        dto.setFileName(attachment.getFileName());
        dto.setFileUrl(attachment.getFileUrl());
        dto.setUploadedAt(attachment.getCreatedAt());

        if (attachment.getTask() != null) {
            dto.setTaskId(attachment.getTask().getId());
            dto.setTaskTitle(attachment.getTask().getTitle());
        }

        if (attachment.getUser() != null) {
            dto.setUploadedById(attachment.getUser().getId());
            dto.setUploadedByName(attachment.getUser().getFullName());
        }

        return dto;
    }
}
