package com.example.demo.service;

import com.example.demo.model.Sprint; // Import Sprint
import com.example.demo.repository.SprintRepository;
import com.example.demo.repository.ProjectRepository; // Import ProjectRepository
import com.example.demo.model.Project; // Import Project

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Import Transactional

import java.time.LocalDate; // Import LocalDate
import java.util.List;
import java.util.Optional;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository; // Inject ProjectRepository

    public SprintService(SprintRepository sprintRepository, ProjectRepository projectRepository) {
        this.sprintRepository = sprintRepository;
        this.projectRepository = projectRepository;
    }

    public List<Sprint> findAll() {
        return sprintRepository.findAll();
    }

    public List<Sprint> findByProjectId(Long projectId) {
        return sprintRepository.findByProjectIdWithTasks(projectId);
    }

    public Optional<Sprint> findById(Long id) {
        return sprintRepository.findById(id);
    }

    @Transactional
    public Sprint save(Sprint sprint) {
        // 1. Retrieve Project
        Project project = projectRepository.findById(sprint.getProject().getId())
                .orElseThrow(() -> new IllegalArgumentException("Project not found with ID: " + sprint.getProject().getId()));

        // 2. Validate Sprint Start Date vs. Project Start Date
        if (sprint.getStartDate().isBefore(project.getStartDate())) {
            throw new IllegalArgumentException("Sprint start date cannot be before project start date.");
        }

        // 3. Validate Sprint Overlap
        List<Sprint> existingSprints = sprintRepository.findByProjectId(project.getId());
        for (Sprint existingSprint : existingSprints) {
            // If updating an existing sprint, exclude itself from the overlap check
            if (sprint.getId() != null && sprint.getId().equals(existingSprint.getId())) {
                continue;
            }

            // Check for overlap
            // (StartA <= EndB) and (EndA >= StartB)
            if (sprint.getStartDate().isBefore(existingSprint.getEndDate()) &&
                sprint.getEndDate().isAfter(existingSprint.getStartDate())) {
                throw new IllegalArgumentException("Sprint dates overlap with an existing sprint: " + existingSprint.getName());
            }
        }

        return sprintRepository.save(sprint);
    }

    public void deleteById(Long id) {
        sprintRepository.deleteById(id);
    }
}
