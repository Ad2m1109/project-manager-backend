package com.example.demo.controller;

import com.example.demo.dto.ProjectDTO;
import com.example.demo.model.AppUser;
import com.example.demo.model.Project;
import com.example.demo.service.AppUserService;
import com.example.demo.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/projects")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final AppUserService appUserService;

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        AppUser currentUser = appUserService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // For now, return all projects. In a real app, filter by membership/ownership
        List<Project> projects = projectService.findAll();

        List<ProjectDTO> dtos = projects.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long id) {
        return projectService.findById(id)
                .map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@RequestBody ProjectDTO projectDTO) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        AppUser currentUser = appUserService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Project project = new Project();
        project.setName(projectDTO.getName());
        project.setDescription(projectDTO.getDescription());
        project.setPriority(projectDTO.getPriority());
        project.setStatus("PLANNED");
        project.setStartDate(projectDTO.getStartDate() != null ? projectDTO.getStartDate() : LocalDate.now());
        project.setFounder(currentUser);

        Project savedProject = projectService.save(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedProject));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable Long id, @RequestBody ProjectDTO projectDTO) {
        return projectService.findById(id)
                .map(existingProject -> {
                    existingProject.setName(projectDTO.getName());
                    existingProject.setDescription(projectDTO.getDescription());
                    existingProject.setPriority(projectDTO.getPriority());
                    if (projectDTO.getStatus() != null) {
                        existingProject.setStatus(projectDTO.getStatus());
                    }
                    if (projectDTO.getStartDate() != null) {
                        existingProject.setStartDate(projectDTO.getStartDate());
                    }
                    return ResponseEntity.ok(convertToDTO(projectService.save(existingProject)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        if (projectService.findById(id).isPresent()) {
            projectService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private ProjectDTO convertToDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setPriority(project.getPriority());
        dto.setStatus(project.getStatus());
        dto.setStartDate(project.getStartDate());
        dto.setFounderId(project.getFounder().getId());
        dto.setFounderName(project.getFounder().getFullName());
        return dto;
    }
}
