package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.TaskDTO;
import com.example.demo.model.AppUser;
import com.example.demo.model.Project;
import com.example.demo.model.Sprint;
import com.example.demo.model.Task;
import com.example.demo.service.AppUserService;
import com.example.demo.service.ProjectService;
import com.example.demo.service.SprintService;
import com.example.demo.service.TaskService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final AppUserService appUserService;
    private final SprintService sprintService;

    // Get all tasks for a specific project
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskDTO>> getProjectTasks(@PathVariable Long projectId) {
        List<Task> tasks = taskService.findByProjectId(projectId);
        List<TaskDTO> dtos = tasks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Get tasks for the authenticated user in a specific project
    @GetMapping("/projects/{projectId}/tasks/me")
    public ResponseEntity<List<TaskDTO>> getMyProjectTasks(@PathVariable Long projectId, Authentication authentication) {
        AppUser currentUser = (AppUser) authentication.getPrincipal();
        List<Task> tasks = taskService.findByProjectIdAndAssigneeId(projectId, currentUser.getId());
        List<TaskDTO> dtos = tasks.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Get a single task by ID
    @GetMapping("/tasks/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return taskService.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create a new task in a project
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskDTO> createTask(
            @PathVariable Long projectId,
            @RequestBody TaskDTO taskDTO) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        AppUser currentUser = appUserService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : "TODO");
        task.setPriority(taskDTO.getPriority() != null ? taskDTO.getPriority() : "MEDIUM");
        task.setProject(project);
        task.setReporter(currentUser);

        // Set assignee if provided
        if (taskDTO.getAssigneeId() != null) {
            AppUser assignee = appUserService.findById(taskDTO.getAssigneeId())
                    .orElseThrow(() -> new RuntimeException("Assignee not found"));
            task.setAssignee(assignee);
        }

        // Set sprint if provided
        if (taskDTO.getSprintId() != null) {
            Sprint sprint = sprintService.findById(taskDTO.getSprintId())
                    .orElseThrow(() -> new RuntimeException("Sprint not found"));
            task.setSprint(sprint);
        }

        Task savedTask = taskService.save(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedTask));
    }

    // Update an existing task
    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long id,
            @RequestBody TaskDTO taskDTO) {

        return taskService.findById(id)
                .map(existingTask -> {
                    existingTask.setTitle(taskDTO.getTitle());
                    existingTask.setDescription(taskDTO.getDescription());
                    existingTask.setStatus(taskDTO.getStatus());
                    existingTask.setPriority(taskDTO.getPriority());

                    // Update assignee if provided
                    if (taskDTO.getAssigneeId() != null) {
                        AppUser assignee = appUserService.findById(taskDTO.getAssigneeId())
                                .orElseThrow(() -> new RuntimeException("Assignee not found"));
                        existingTask.setAssignee(assignee);
                    } else {
                        existingTask.setAssignee(null);
                    }

                    // Update sprint if provided
                    if (taskDTO.getSprintId() != null) {
                        Sprint sprint = sprintService.findById(taskDTO.getSprintId())
                                .orElseThrow(() -> new RuntimeException("Sprint not found"));
                        existingTask.setSprint(sprint);
                    } else {
                        existingTask.setSprint(null);
                    }

                    return ResponseEntity.ok(convertToDTO(taskService.save(existingTask)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Update task status (for Kanban board drag-and-drop)
    @PatchMapping("/tasks/{id}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody TaskDTO taskDTO) {

        return taskService.findById(id)
                .map(existingTask -> {
                    existingTask.setStatus(taskDTO.getStatus());
                    return ResponseEntity.ok(convertToDTO(taskService.save(existingTask)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete a task
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (taskService.findById(id).isPresent()) {
            taskService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Helper method to convert Task entity to TaskDTO
    private TaskDTO convertToDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setCreatedAt(task.getCreatedAt());

        if (task.getProject() != null) {
            dto.setProjectId(task.getProject().getId());
            dto.setProjectName(task.getProject().getName());
        }

        if (task.getSprint() != null) {
            dto.setSprintId(task.getSprint().getId());
            dto.setSprintName(task.getSprint().getName());
        }

        if (task.getAssignee() != null) {
            dto.setAssigneeId(task.getAssignee().getId());
            dto.setAssigneeName(task.getAssignee().getFullName());
        }

        if (task.getReporter() != null) {
            dto.setReporterId(task.getReporter().getId());
            dto.setReporterName(task.getReporter().getFullName());
        }

        return dto;
    }
}
