package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ProjectDTO;
import com.example.demo.model.AppUser;
import com.example.demo.model.Project;
import com.example.demo.service.AppUserService;
import com.example.demo.service.ProjectService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/projects")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final com.example.demo.service.ProjectMemberService projectMemberService;
    private final AppUserService appUserService;

    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        AppUser currentUser = appUserService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Project> projects;

        if ("FOUNDER".equalsIgnoreCase(currentUser.getRoleType())) {
            // Return only projects created by this founder
            projects = projectService.findAll().stream()
                    .filter(p -> p.getFounder() != null && p.getFounder().getId().equals(currentUser.getId()))
                    .collect(Collectors.toList());
        } else if ("EMPLOYEE".equalsIgnoreCase(currentUser.getRoleType())) {
            // Return projects the employee has joined
            List<com.example.demo.model.ProjectMember> memberships = projectMemberService.findByUserId(currentUser.getId());
            projects = memberships.stream().map(com.example.demo.model.ProjectMember::getProject).collect(Collectors.toList());
        } else {
            // Default: return no projects for unknown role
            projects = List.of();
        }

        List<ProjectDTO> dtos = projects.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

        @GetMapping("/founder")
        public ResponseEntity<List<ProjectDTO>> getFounderProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        AppUser currentUser = appUserService.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        List<Project> projects = projectService.findAll().stream()
            .filter(p -> p.getFounder() != null && p.getFounder().getId().equals(currentUser.getId()))
            .collect(Collectors.toList());

        List<ProjectDTO> dtos = projects.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
        }

        @GetMapping("/employee")
        public ResponseEntity<List<ProjectDTO>> getEmployeeProjects() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        AppUser currentUser = appUserService.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // find project memberships for this user and return the projects
        List<com.example.demo.model.ProjectMember> memberships = projectMemberService.findByUserId(currentUser.getId());

        List<Project> projects = memberships.stream().map(com.example.demo.model.ProjectMember::getProject).collect(Collectors.toList());

        List<ProjectDTO> dtos = projects.stream().map(this::convertToDTO).collect(Collectors.toList());
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
        // Only Founders can create projects
        if (!"FOUNDER".equalsIgnoreCase(currentUser.getRoleType())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

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
