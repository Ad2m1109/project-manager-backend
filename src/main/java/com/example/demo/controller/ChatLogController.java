package com.example.demo.controller;

import com.example.demo.dto.ChatLogRequest;
import com.example.demo.model.AppUser;
import com.example.demo.model.ChatLog;
import com.example.demo.model.Project;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.service.ChatLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/chat-logs")
@RequiredArgsConstructor
public class ChatLogController {

    private final ChatLogService chatLogService;
    private final AppUserRepository appUserRepository;
    private final ProjectRepository projectRepository;

    @PostMapping
    public ResponseEntity<ChatLog> saveChatLog(@RequestBody ChatLogRequest request) {
        AppUser user = appUserRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found with ID: " + request.getProjectId()));

        ChatLog chatLog = ChatLog.builder()
                .user(user)
                .project(project)
                .date(request.getDate() != null ? request.getDate() : LocalDateTime.now())
                .content(request.getContent())
                .build();

        ChatLog savedChatLog = chatLogService.saveChatLog(chatLog);
        return new ResponseEntity<>(savedChatLog, HttpStatus.CREATED);
    }
}