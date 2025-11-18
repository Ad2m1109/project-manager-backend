package com.example.demo.controller;

import com.example.demo.model.ProjectMember;
import com.example.demo.service.ProjectMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/project-members")
public class ProjectMemberController {

    private final ProjectMemberService projectMemberService;

    @Autowired
    public ProjectMemberController(ProjectMemberService projectMemberService) {
        this.projectMemberService = projectMemberService;
    }

    @GetMapping
    public List<ProjectMember> getAllProjectMembers() {
        return projectMemberService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectMember> getProjectMemberById(@PathVariable Long id) {
        ProjectMember projectMember = projectMemberService.findById(id);
        return ResponseEntity.ok(projectMember);
    }

    @PostMapping
    public ProjectMember createProjectMember(@RequestBody ProjectMember projectMember) {
        return projectMemberService.save(projectMember);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectMember> updateProjectMember(@PathVariable Long id, @RequestBody ProjectMember projectMemberDetails) {
        ProjectMember projectMember = projectMemberService.findById(id);
        // Update fields here
        // projectMember.set...
        return ResponseEntity.ok(projectMemberService.save(projectMember));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProjectMember(@PathVariable Long id) {
        projectMemberService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
