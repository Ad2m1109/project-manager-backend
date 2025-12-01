package com.example.demo.controller;

import com.example.demo.dto.ProjectMemberDTO;
import com.example.demo.model.AppUser;
import com.example.demo.model.Project;
import com.example.demo.model.ProjectMember;
import com.example.demo.model.ProjectMemberId;
import com.example.demo.service.AppUserService;
import com.example.demo.service.ProjectMemberService;
import com.example.demo.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;
    private final ProjectService projectService;
    private final AppUserService appUserService;

    // Get all members for a specific project
    @GetMapping("/projects/{projectId}/members")
    public ResponseEntity<List<ProjectMemberDTO>> getProjectMembers(@PathVariable Long projectId) {
        List<ProjectMember> members = projectMemberService.findByProjectId(projectId);
        List<ProjectMemberDTO> dtos = members.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Add a member to a project
    @PostMapping("/projects/{projectId}/members")
    public ResponseEntity<ProjectMemberDTO> addProjectMember(
            @PathVariable Long projectId,
            @RequestBody ProjectMemberDTO memberDTO) {

        Project project = projectService.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        AppUser user = appUserService.findById(memberDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProjectMemberId id = new ProjectMemberId(memberDTO.getUserId(), projectId);
        ProjectMember member = new ProjectMember();
        member.setId(id);
        member.setUser(user);
        member.setProject(project);
        member.setRoleInProject(memberDTO.getRoleInProject() != null ? memberDTO.getRoleInProject() : "MEMBER");

        ProjectMember savedMember = projectMemberService.save(member);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedMember));
    }

    // Update a member's role in a project
    @PutMapping("/projects/{projectId}/members/{userId}")
    public ResponseEntity<ProjectMemberDTO> updateProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long userId,
            @RequestBody ProjectMemberDTO memberDTO) {

        ProjectMemberId id = new ProjectMemberId(userId, projectId);
        return projectMemberService.findById(id)
                .map(existingMember -> {
                    existingMember.setRoleInProject(memberDTO.getRoleInProject());
                    return ResponseEntity.ok(convertToDTO(projectMemberService.save(existingMember)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Remove a member from a project
    @DeleteMapping("/projects/{projectId}/members/{userId}")
    public ResponseEntity<Void> removeProjectMember(
            @PathVariable Long projectId,
            @PathVariable Long userId) {

        ProjectMemberId id = new ProjectMemberId(userId, projectId);
        if (projectMemberService.findById(id).isPresent()) {
            projectMemberService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Helper method to convert ProjectMember entity to ProjectMemberDTO
    private ProjectMemberDTO convertToDTO(ProjectMember member) {
        ProjectMemberDTO dto = new ProjectMemberDTO();

        if (member.getUser() != null) {
            dto.setUserId(member.getUser().getId());
            dto.setUserName(member.getUser().getFullName());
            dto.setUserEmail(member.getUser().getEmail());
        }

        if (member.getProject() != null) {
            dto.setProjectId(member.getProject().getId());
            dto.setProjectName(member.getProject().getName());
        }

        dto.setRoleInProject(member.getRoleInProject());

        return dto;
    }
}
