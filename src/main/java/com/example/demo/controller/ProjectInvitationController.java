package com.example.demo.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ProjectInvitationDTO;
import com.example.demo.model.AppUser;
import com.example.demo.service.ProjectInvitationService;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "*")
public class ProjectInvitationController {

    @Autowired
    private ProjectInvitationService invitationService;

    @PostMapping("/projects/{projectId}/invitations")
    public ResponseEntity<ProjectInvitationDTO> sendInvitation(
            @PathVariable Long projectId,
            @RequestBody Map<String, Long> request,
            Authentication authentication) {

        AppUser currentUser = (AppUser) authentication.getPrincipal();
        Long invitedUserId = request.get("userId");

        ProjectInvitationDTO invitation = invitationService.sendInvitation(projectId, invitedUserId,
                currentUser.getId());
        return ResponseEntity.ok(invitation);
    }

    @PostMapping("/projects/{projectId}/invitations/email")
    public ResponseEntity<ProjectInvitationDTO> sendInvitationByEmail(
            @PathVariable Long projectId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        AppUser currentUser = (AppUser) authentication.getPrincipal();
        String email = request.get("email");
        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ProjectInvitationDTO invitation = invitationService.sendInvitationByEmail(projectId, email, currentUser.getId());
        return ResponseEntity.ok(invitation);
    }

    @GetMapping("/invitations/my-invitations")
    public ResponseEntity<List<ProjectInvitationDTO>> getMyInvitations(Authentication authentication) {
        AppUser currentUser = (AppUser) authentication.getPrincipal();
        List<ProjectInvitationDTO> invitations = invitationService.getMyInvitations(currentUser.getId());
        return ResponseEntity.ok(invitations);
    }

    @PostMapping("/invitations/{invitationId}/accept")
    public ResponseEntity<ProjectInvitationDTO> acceptInvitation(
            @PathVariable Long invitationId,
            Authentication authentication) {

        AppUser currentUser = (AppUser) authentication.getPrincipal();
        ProjectInvitationDTO invitation = invitationService.acceptInvitation(invitationId, currentUser.getId());
        return ResponseEntity.ok(invitation);
    }

    @PostMapping("/invitations/{invitationId}/reject")
    public ResponseEntity<ProjectInvitationDTO> rejectInvitation(
            @PathVariable Long invitationId,
            Authentication authentication) {

        AppUser currentUser = (AppUser) authentication.getPrincipal();
        ProjectInvitationDTO invitation = invitationService.rejectInvitation(invitationId, currentUser.getId());
        return ResponseEntity.ok(invitation);
    }

    @GetMapping("/projects/{projectId}/invitations")
    public ResponseEntity<List<ProjectInvitationDTO>> getProjectInvitations(@PathVariable Long projectId) {
        List<ProjectInvitationDTO> invitations = invitationService.getProjectInvitations(projectId);
        return ResponseEntity.ok(invitations);
    }
}
