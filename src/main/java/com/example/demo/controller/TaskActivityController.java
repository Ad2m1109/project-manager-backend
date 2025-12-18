package com.example.demo.controller;

import com.example.demo.dto.TaskActivityDTO;
import com.example.demo.model.TaskActivity;
import com.example.demo.service.TaskActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tasks/{taskId}/activities")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TaskActivityController {
    private final TaskActivityService taskActivityService;

    @GetMapping
    public ResponseEntity<List<TaskActivityDTO>> getTaskActivities(@PathVariable Long taskId) {
        List<TaskActivity> activities = taskActivityService.findByTaskId(taskId);
        List<TaskActivityDTO> dtos = activities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    private TaskActivityDTO convertToDTO(TaskActivity activity) {
        TaskActivityDTO dto = new TaskActivityDTO();
        dto.setId(activity.getId());
        dto.setTaskId(activity.getTask().getId());
        dto.setUserId(activity.getUser().getId());
        dto.setUserName(activity.getUser().getFullName());
        dto.setAction(activity.getAction());
        dto.setOldValue(activity.getOldValue());
        dto.setNewValue(activity.getNewValue());
        dto.setCreatedAt(activity.getCreatedAt());
        return dto;
    }
}
