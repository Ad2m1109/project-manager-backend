package com.example.demo.repository;

import com.example.demo.model.ProjectInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {
    List<ProjectInvitation> findByInvitedUserIdAndStatus(Long userId, String status);

    List<ProjectInvitation> findByProjectId(Long projectId);

    Optional<ProjectInvitation> findByProjectIdAndInvitedUserIdAndStatus(Long projectId, Long userId, String status);
}
