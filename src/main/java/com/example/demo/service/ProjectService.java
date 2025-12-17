package com.example.demo.service;

import com.example.demo.model.Project;
import com.example.demo.repository.ProjectRepository;
import com.example.demo.repository.TaskRepository;
import com.example.demo.repository.SprintRepository;
import com.example.demo.model.Task;
import com.example.demo.model.Sprint;
import com.example.demo.dto.ProjectDashboardDTO;
import com.example.demo.dto.SprintDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    public void deleteById(Long id) {
        projectRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProjectDashboardDTO getDashboardData(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        List<Task> allTasks = taskRepository.findByProjectId(projectId);
        List<Sprint> allSprints = sprintRepository.findByProjectId(projectId);

        ProjectDashboardDTO dashboard = new ProjectDashboardDTO();

        // Overall Progress
        long totalTasks = allTasks.size();
        long completedTasks = allTasks.stream().filter(t -> "DONE".equalsIgnoreCase(t.getStatus())).count();
        dashboard.setOverallProgress(totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);

        // Task Distribution
        Map<String, Long> distribution = allTasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
        dashboard.setTaskDistribution(distribution);

        // Blocked and Overdue
        dashboard
                .setBlockedTasksCount(allTasks.stream().filter(t -> "BLOCKED".equalsIgnoreCase(t.getStatus())).count());
        // For overdue, we'd need a dueDate in Task. For now, let's assume no overdue.
        dashboard.setOverdueTasksCount(0);

        // Active Sprint
        LocalDate now = LocalDate.now();
        Optional<Sprint> activeSprintOpt = allSprints.stream()
                .filter(s -> (s.getStartDate() != null && !s.getStartDate().isAfter(now)) &&
                        (s.getEndDate() != null && !s.getEndDate().isBefore(now)))
                .findFirst();

        if (activeSprintOpt.isPresent()) {
            Sprint activeSprint = activeSprintOpt.get();
            SprintDTO sprintDTO = new SprintDTO();
            sprintDTO.setId(activeSprint.getId());
            sprintDTO.setName(activeSprint.getName());
            sprintDTO.setGoal(activeSprint.getGoal());
            sprintDTO.setStartDate(activeSprint.getStartDate());
            sprintDTO.setEndDate(activeSprint.getEndDate());

            long sprintTotal = activeSprint.getTasks().size();
            long sprintCompleted = activeSprint.getTasks().stream().filter(t -> "DONE".equalsIgnoreCase(t.getStatus()))
                    .count();
            sprintDTO.setTaskCount((int) sprintTotal);
            sprintDTO.setProgress(sprintTotal > 0 ? (double) sprintCompleted / sprintTotal * 100 : 0);
            dashboard.setActiveSprint(sprintDTO);
        }

        return dashboard;
    }
}
