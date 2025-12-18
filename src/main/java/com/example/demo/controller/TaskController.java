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
import com.example.demo.service.TaskActivityService;
import com.example.demo.service.EmailService;
import com.example.demo.model.TaskActivity;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.demo.repository.TaskSpecification;
import org.springframework.data.jpa.domain.Specification;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final AppUserService appUserService;
    private final SprintService sprintService;
    private final TaskActivityService taskActivityService;
    private final EmailService emailService;

    @GetMapping("/projects/{projectId}/tasks/filter")
    @PreAuthorize("hasAnyAuthority('FOUNDER', 'EMPLOYEE')")
    public ResponseEntity<List<TaskDTO>> getFilteredTasks(
            @PathVariable Long projectId,
            @RequestParam(required = false) Long assigneeId,
            @RequestParam(required = false) Long sprintId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) boolean unassigned) {

        Specification<Task> spec = Specification.where(TaskSpecification.hasProjectId(projectId));

        if (assigneeId != null) {
            spec = spec.and(TaskSpecification.hasAssigneeId(assigneeId));
        }
        if (sprintId != null) {
            if (sprintId == -1L) {
                spec = spec.and(TaskSpecification.isBacklog());
            } else {
                spec = spec.and(TaskSpecification.hasSprintId(sprintId));
            }
        }
        if (status != null && !status.isEmpty()) {
            spec = spec.and(TaskSpecification.hasStatus(status));
        }
        if (priority != null && !priority.isEmpty()) {
            spec = spec.and(TaskSpecification.hasPriority(priority));
        }
        if (unassigned) {
            spec = spec.and(TaskSpecification.isUnassigned());
        }

        List<Task> tasks = taskService.findAll(spec);
        List<TaskDTO> dtos = tasks.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

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
    public ResponseEntity<List<TaskDTO>> getMyProjectTasks(@PathVariable Long projectId,
            Authentication authentication) {
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

        if (!"FOUNDER".equals(currentUser.getRoleType())) {
            return ResponseEntity.status(403).build();
        }

        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Task task = new Task();
        task.setTitle(taskDTO.getTitle());
        task.setDescription(taskDTO.getDescription());
        task.setStatus(taskDTO.getStatus() != null ? taskDTO.getStatus() : "PLANNED");
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

        // Send email to assignee if assigned
        if (savedTask.getAssignee() != null) {
            String subject = "New Task Assigned: " + savedTask.getTitle();
            String text = "Dear " + savedTask.getAssignee().getFullName() + ",\n\n"
                        + "A new task '" + savedTask.getTitle() + "' has been assigned to you in project '" + project.getName() + "'.\n"
                        + "Description: " + savedTask.getDescription() + "\n"
                        + "Status: " + savedTask.getStatus() + "\n"
                        + "Priority: " + savedTask.getPriority() + "\n\n"
                        + "Please log in to the Project Manager application to view the task details.\n\n"
                        + "Regards,\nThe Project Manager Team";
            emailService.sendSimpleEmail(savedTask.getAssignee().getEmail(), subject, text);
        }

        // Record activity
        TaskActivity activity = new TaskActivity();
        activity.setTask(savedTask);
        activity.setUser(currentUser);
        activity.setAction("TASK_CREATED");
        activity.setNewValue(savedTask.getTitle());
        taskActivityService.save(activity);

        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedTask));
    }

    // Update an existing task
    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable Long id,
            @RequestBody TaskDTO taskDTO) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = (AppUser) auth.getPrincipal();
        if (!"FOUNDER".equals(currentUser.getRoleType())) {
            return ResponseEntity.status(403).build();
        }

        return taskService.findById(id)
                .map(existingTask -> {
                    String oldStatus = existingTask.getStatus();
                    AppUser oldAssignee = existingTask.getAssignee();
                    String oldAssigneeName = oldAssignee != null ? oldAssignee.getFullName() : "Unassigned";

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

                    Task savedTask = taskService.save(existingTask);

                    // Record activities
                    if (!oldStatus.equals(savedTask.getStatus())) {
                        recordActivity(savedTask, currentUser, "STATUS_CHANGE", oldStatus, savedTask.getStatus());
                    }
                    AppUser newAssignee = savedTask.getAssignee();
                    String newAssigneeName = newAssignee != null ? newAssignee.getFullName() : "Unassigned";

                    if (!oldAssigneeName.equals(newAssigneeName)) {
                        recordActivity(savedTask, currentUser, "ASSIGNEE_CHANGE", oldAssigneeName, newAssigneeName);
                        // Send email to new assignee if assigned
                        if (newAssignee != null) {
                            String subject = "Task Assignment Update: " + savedTask.getTitle();
                            String text = "Dear " + newAssignee.getFullName() + ",\n\n"
                                        + "The task '" + savedTask.getTitle() + "' in project '" + savedTask.getProject().getName() + "' has been assigned to you.\n"
                                        + "Description: " + savedTask.getDescription() + "\n"
                                        + "Status: " + savedTask.getStatus() + "\n"
                                        + "Priority: " + savedTask.getPriority() + "\n\n"
                                        + "Please log in to the Project Manager application to view the task details.\n\n"
                                        + "Regards,\nThe Project Manager Team";
                            emailService.sendSimpleEmail(newAssignee.getEmail(), subject, text);
                        }
                        // Optionally, notify old assignee if they were removed
                        if (oldAssignee != null && newAssignee == null) {
                            String subject = "Task Unassigned: " + savedTask.getTitle();
                            String text = "Dear " + oldAssignee.getFullName() + ",\n\n"
                                        + "The task '" + savedTask.getTitle() + "' in project '" + savedTask.getProject().getName() + "' has been unassigned from you.\n\n"
                                        + "Regards,\nThe Project Manager Team";
                            emailService.sendSimpleEmail(oldAssignee.getEmail(), subject, text);
                        }
                    }

                    return ResponseEntity.ok(convertToDTO(savedTask));
                })
                .orElse(ResponseEntity.notFound().build());

    }

    // Update task status (for Kanban board drag-and-drop)
    @PutMapping("/tasks/{id}/status")
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody TaskDTO taskDTO) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        AppUser currentUser = appUserService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return taskService.findById(id)
                .map(existingTask -> {
                    // Authorization check:
                    boolean isProjectFounder = existingTask.getProject().getFounder().getId().equals(currentUser.getId());
                    boolean isAssignee = existingTask.getAssignee() != null && existingTask.getAssignee().getId().equals(currentUser.getId());

                    if (!isProjectFounder && !isAssignee) {
                         return new ResponseEntity<TaskDTO>(HttpStatus.FORBIDDEN);
                    }

                    String oldStatus = existingTask.getStatus();
                    existingTask.setStatus(taskDTO.getStatus());
                    Task updatedTask = taskService.save(existingTask);

                    // Record activity
                    if (!oldStatus.equals(updatedTask.getStatus())) {
                        recordActivity(updatedTask, currentUser, "STATUS_CHANGE", oldStatus, updatedTask.getStatus());
                    }

                    return ResponseEntity.ok(convertToDTO(updatedTask));
                })
                .orElse(new ResponseEntity<TaskDTO>(HttpStatus.NOT_FOUND));
    }

    // Delete a task
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = (AppUser) auth.getPrincipal();
        if (!"FOUNDER".equals(currentUser.getRoleType())) {
            return ResponseEntity.status(403).build();
        }

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

    private void recordActivity(Task task, AppUser user, String action, String oldValue, String newValue) {
        if (user == null)
            return;
        TaskActivity activity = new TaskActivity();
        activity.setTask(task);
        activity.setUser(user);
        activity.setAction(action);
        activity.setOldValue(oldValue);
        activity.setNewValue(newValue);
        taskActivityService.save(activity);
    }
}
