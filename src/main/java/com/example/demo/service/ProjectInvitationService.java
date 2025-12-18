package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.dto.ProjectInvitationDTO;
import com.example.demo.model.AppUser;
import com.example.demo.model.Project;
import com.example.demo.model.ProjectInvitation;
import com.example.demo.model.ProjectMember;
import com.example.demo.model.ProjectMemberId;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.ProjectInvitationRepository;
import com.example.demo.repository.ProjectMemberRepository;
import com.example.demo.repository.ProjectRepository;

@Service
public class ProjectInvitationService {

    private final ProjectInvitationRepository invitationRepository;
    private final ProjectRepository projectRepository;
    private final AppUserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final EmailService emailService;

    public ProjectInvitationService(ProjectInvitationRepository invitationRepository,
                                    ProjectRepository projectRepository,
                                    AppUserRepository userRepository,
                                    ProjectMemberRepository projectMemberRepository,
                                    EmailService emailService) {
        this.invitationRepository = invitationRepository;
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.emailService = emailService;
    }

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

        String subject = "Project Invitation: " + project.getName();
        String text = "Dear " + invitedUser.getFullName() + ",\n\n"
                    + "You have been invited to the project '" + project.getName() + "' by " + invitedBy.getFullName() + ".\n"
                    + "Please log in to the Project Manager application to accept or reject the invitation.\n\n"
                    + "Regards,\nThe Project Manager Team";
        emailService.sendSimpleEmail(invitedUser.getEmail(), subject, text);

        return convertToDTO(saved);
    }

    // Send invitation by email: create user if not exists (as EMPLOYEE) then send invitation
    public ProjectInvitationDTO sendInvitationByEmail(Long projectId, String email, Long invitedById) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        AppUser invitedUser = userRepository.findByEmail(email).orElse(null);
        if (invitedUser == null) {
            // create a lightweight user for the invited email with a random password
            AppUser newUser = new AppUser();
            newUser.setFullName(email);
            newUser.setEmail(email);
            // set a random password placeholder - should be changed by invitee
            newUser.setPassword(java.util.UUID.randomUUID().toString());
            newUser.setRoleType("EMPLOYEE");
            invitedUser = userRepository.save(newUser);
        }

        AppUser invitedBy = userRepository.findById(invitedById)
                .orElseThrow(() -> new RuntimeException("Inviter not found"));

        if (invitationRepository.findByProjectIdAndInvitedUserIdAndStatus(projectId, invitedUser.getId(), "PENDING")
                .isPresent()) {
            throw new RuntimeException("Invitation already sent");
        }

        ProjectInvitation invitation = new ProjectInvitation();
        invitation.setProject(project);
        invitation.setInvitedUser(invitedUser);
        invitation.setInvitedBy(invitedBy);
        invitation.setStatus("PENDING");

        ProjectInvitation saved = invitationRepository.save(invitation);

        String subject = "Project Invitation: " + project.getName();
        String text = "Dear " + invitedUser.getFullName() + ",\n\n"
                    + "You have been invited to the project '" + project.getName() + "' by " + invitedBy.getFullName() + ".\n"
                    + "Please log in to the Project Manager application to accept or reject the invitation.\n\n"
                    + "Regards,\nThe Project Manager Team";
        emailService.sendSimpleEmail(invitedUser.getEmail(), subject, text);

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
        // Add the user to project members if not already present
        Long pid = saved.getProject().getId();
        Long uid = saved.getInvitedUser().getId();
        ProjectMemberId pmId = new ProjectMemberId(uid, pid);
        boolean exists = projectMemberRepository.findById(pmId).isPresent();
        if (!exists) {
            ProjectMember pm = new ProjectMember();
            pm.setId(pmId);
            pm.setUser(saved.getInvitedUser());
            pm.setProject(saved.getProject());
            pm.setRoleInProject("MEMBER");
            projectMemberRepository.save(pm);
        }
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
