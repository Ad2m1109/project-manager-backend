package com.example.demo.controller;
import com.example.demo.dto.SprintDTO;
import com.example.demo.model.Project;
import com.example.demo.model.Sprint;
import com.example.demo.service.ProjectService;
import com.example.demo.service.SprintService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.example.demo.model.AppUser;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SprintController {

    private final SprintService sprintService;
    private final ProjectService projectService;

    // Get all sprints for a specific project
    @GetMapping("/projects/{projectId}/sprints")
    public ResponseEntity<List<SprintDTO>> getProjectSprints(@PathVariable Long projectId) {
        List<Sprint> sprints = sprintService.findByProjectId(projectId);
        List<SprintDTO> dtos = sprints.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Get a single sprint by ID
    @GetMapping("/sprints/{id}")
    public ResponseEntity<SprintDTO> getSprintById(@PathVariable Long id) {
        return sprintService.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Create a new sprint in a project
    @PostMapping("/projects/{projectId}/sprints")
    public ResponseEntity<SprintDTO> createSprint(
            @PathVariable Long projectId,
            @RequestBody SprintDTO sprintDTO) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = (AppUser) auth.getPrincipal();
        if (!"FOUNDER".equals(currentUser.getRoleType())) {
            return ResponseEntity.status(403).build();
        }

        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Sprint sprint = new Sprint();
        sprint.setName(sprintDTO.getName());
        sprint.setGoal(sprintDTO.getGoal());
        sprint.setStartDate(sprintDTO.getStartDate());
        sprint.setEndDate(sprintDTO.getEndDate());
        sprint.setProject(project);

        Sprint savedSprint = sprintService.save(sprint);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedSprint));
    }

    // Update an existing sprint
    @PutMapping("/sprints/{id}")
    public ResponseEntity<SprintDTO> updateSprint(
            @PathVariable Long id,
            @RequestBody SprintDTO sprintDTO) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = (AppUser) auth.getPrincipal();
        if (!"FOUNDER".equals(currentUser.getRoleType())) {
            return ResponseEntity.status(403).build();
        }

        return sprintService.findById(id)
                .map(existingSprint -> {
                    existingSprint.setName(sprintDTO.getName());
                    existingSprint.setGoal(sprintDTO.getGoal());
                    existingSprint.setStartDate(sprintDTO.getStartDate());
                    existingSprint.setEndDate(sprintDTO.getEndDate());
                    return ResponseEntity.ok(convertToDTO(sprintService.save(existingSprint)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete a sprint
    @DeleteMapping("/sprints/{id}")
    public ResponseEntity<Void> deleteSprint(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUser currentUser = (AppUser) auth.getPrincipal();
        if (!"FOUNDER".equals(currentUser.getRoleType())) {
            return ResponseEntity.status(403).build();
        }

        if (sprintService.findById(id).isPresent()) {
            sprintService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Helper method to convert Sprint entity to SprintDTO
    private SprintDTO convertToDTO(Sprint sprint) {
        SprintDTO dto = new SprintDTO();
        dto.setId(sprint.getId());
        dto.setName(sprint.getName());
        dto.setGoal(sprint.getGoal());
        dto.setStartDate(sprint.getStartDate());
        dto.setEndDate(sprint.getEndDate());

        if (sprint.getProject() != null) {
            dto.setProjectId(sprint.getProject().getId());
            dto.setProjectName(sprint.getProject().getName());
        }

        if (sprint.getTasks() != null) {
            dto.setTaskCount(sprint.getTasks().size());
        } else {
            dto.setTaskCount(0);
        }

        return dto;
    }
}
