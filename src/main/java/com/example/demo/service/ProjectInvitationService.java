package com.example.demo.service;

import com.example.demo.dto.ProjectInvitationDTO;
import com.example.demo.model.AppUser;
import com.example.demo.model.Project;
import com.example.demo.model.ProjectInvitation;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.ProjectInvitationRepository;
import com.example.demo.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectInvitationService {

    @Autowired
    private ProjectInvitationRepository invitationRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private AppUserRepository userRepository;

    public ProjectInvitationDTO sendInvitation(Long projectId, Long invitedUserId, Long invitedById) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        AppUser invitedUser = userRepository.findById(invitedUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AppUser invitedBy = userRepository.findById(invitedById)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));

        // Check if invitation already exists
        if (invitationRepository.findByProjectIdAndInvitedUserIdAndStatus(projectId, invitedUserId, "PENDING")
                .isPresent()) {
            throw new RuntimeException("Invitation already sent");
        }

        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setProject(project);
        invitation.setInvitedUser(invitedUser);
        invitation.setInvitedBy(invitedBy);
        invitation.setStatus("PENDING");

        ProjectInvitation saved = invitationRepository.save(invitation);
        return convertToDTO(saved);
    }

    public List<ProjectInvitationDTO> getMyInvitations(Long userId) {
        return invitationRepository.findByInvitedUserIdAndStatus(userId, "PENDING")
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProjectInvitationDTO acceptInvitation(Long invitationId, Long userId) {
        ProjectInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!invitation.getInvitedUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new RuntimeException("Invitation already responded to");
        }

        invitation.setStatus("ACCEPTED");
        invitation.setRespondedAt(LocalDateTime.now());

        ProjectInvitation saved = invitationRepository.save(invitation);
        return convertToDTO(saved);
    }

    public ProjectInvitationDTO rejectInvitation(Long invitationId, Long userId) {
        ProjectInvitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new RuntimeException("Invitation not found"));

        if (!invitation.getInvitedUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (!"PENDING".equals(invitation.getStatus())) {
            throw new RuntimeException("Invitation already responded to");
        }

        invitation.setStatus("REJECTED");
        invitation.setRespondedAt(LocalDateTime.now());

        ProjectInvitation saved = invitationRepository.save(invitation);
        return convertToDTO(saved);
    }

    public List<ProjectInvitationDTO> getProjectInvitations(Long projectId) {
        return invitationRepository.findByProjectId(projectId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private ProjectInvitationDTO convertToDTO(ProjectInvitation invitation) {
        ProjectInvitationDTO dto = new ProjectInvitationDTO();
        dto.setId(invitation.getId());
        dto.setProjectId(invitation.getProject().getId());
        dto.setProjectName(invitation.getProject().getName());
        dto.setInvitedUserId(invitation.getInvitedUser().getId());
        dto.setInvitedUserName(invitation.getInvitedUser().getFullName());
        dto.setInvitedById(invitation.getInvitedBy().getId());
        dto.setInvitedByName(invitation.getInvitedBy().getFullName());
        dto.setStatus(invitation.getStatus());
        dto.setCreatedAt(invitation.getCreatedAt());
        dto.setRespondedAt(invitation.getRespondedAt());
        return dto;
    }
}
