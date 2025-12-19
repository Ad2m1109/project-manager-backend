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

        LocalDate now = LocalDate.now();

        ProjectDashboardDTO dashboard = new ProjectDashboardDTO();

        // Overall Progress
        long totalTasks = allTasks.size();
        long completedTasks = allTasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();
        dashboard.setOverallProgress(totalTasks > 0 ? (double) completedTasks / totalTasks * 100 : 0);

        // Task Distribution
        Map<String, Long> distribution = allTasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
        dashboard.setTaskDistribution(distribution);

        // Blocked and Overdue
        dashboard
                .setBlockedTasksCount(allTasks.stream().filter(t -> "BLOCKED".equalsIgnoreCase(t.getStatus())).count());
        long overdueTasksCount = allTasks.stream()
                .filter(task -> task.getSprint() != null && task.getSprint().getEndDate() != null)
                .filter(task -> task.getSprint().getEndDate().isBefore(now) && !"COMPLETED".equalsIgnoreCase(task.getStatus()))
                .count();
        dashboard.setOverdueTasksCount(overdueTasksCount);

        // Active Sprint

        // 1. Try to find a current active sprint that is NOT completed
        Optional<Sprint> activeSprintOpt = allSprints.stream()
                .filter(s -> s.getStartDate() != null && s.getEndDate() != null)
                .filter(s -> !s.getStartDate().isAfter(now) && !s.getEndDate().isBefore(now)) // Current date range
                .filter(s -> !"COMPLETED".equalsIgnoreCase(s.getStatus())) // Not completed
                .sorted((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()))
                .findFirst();

        // 2. If no current active sprint, look for the next upcoming sprint
        if (activeSprintOpt.isEmpty()) {
            activeSprintOpt = allSprints.stream()
                    .filter(s -> s.getStartDate() != null && s.getStartDate().isAfter(now)) // Future start date
                    .filter(s -> !"COMPLETED".equalsIgnoreCase(s.getStatus())) // Not completed
                    .sorted((s1, s2) -> s1.getStartDate().compareTo(s2.getStartDate()))
                    .findFirst();
        }

        if (activeSprintOpt.isPresent()) {
            Sprint activeSprint = activeSprintOpt.get();
            SprintDTO sprintDTO = new SprintDTO();
            sprintDTO.setId(activeSprint.getId());
            sprintDTO.setName(activeSprint.getName());
            sprintDTO.setGoal(activeSprint.getGoal());
            sprintDTO.setStartDate(activeSprint.getStartDate());
            sprintDTO.setEndDate(activeSprint.getEndDate());

            long sprintTotal = activeSprint.getTasks().size();
            long sprintCompleted = activeSprint.getTasks().stream()
                    .filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus()))
                    .count();
            sprintDTO.setTaskCount((int) sprintTotal);
            sprintDTO.setProgress(sprintTotal > 0 ? (double) sprintCompleted / sprintTotal * 100 : 0);

            // Populate tasks
            List<com.example.demo.dto.TaskDTO> taskDTOs = activeSprint.getTasks().stream()
                    .map(this::convertToTaskDTO)
                    .collect(Collectors.toList());
            sprintDTO.setTasks(taskDTOs);

            dashboard.setActiveSprint(sprintDTO);
        }

        // Populate allSprints for the timeline
        List<SprintDTO> allSprintDTOs = allSprints.stream()
                .map(this::convertToSprintDTO)
                .collect(Collectors.toList());
        dashboard.setAllSprints(allSprintDTOs);

        return dashboard;
    }

    private com.example.demo.dto.TaskDTO convertToTaskDTO(Task task) {
        com.example.demo.dto.TaskDTO dto = new com.example.demo.dto.TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setProjectId(task.getProject().getId());
        dto.setProjectName(task.getProject().getName());
        if (task.getSprint() != null) {
            dto.setSprintId(task.getSprint().getId());
            dto.setSprintName(task.getSprint().getName());
        }
        if (task.getAssignee() != null) {
            dto.setAssigneeId(task.getAssignee().getId());
            dto.setAssigneeName(task.getAssignee().getFullName());
        }
        if (task.getReporter() != null) {
            dto.setReporterId(task.getReporter().getId());
            dto.setReporterName(task.getReporter().getFullName());
        }
        dto.setCreatedAt(task.getCreatedAt());
        return dto;
    }

    private SprintDTO convertToSprintDTO(Sprint sprint) {
        SprintDTO dto = new SprintDTO();
        dto.setId(sprint.getId());
        dto.setName(sprint.getName());
        dto.setGoal(sprint.getGoal());
        dto.setStartDate(sprint.getStartDate());
        dto.setEndDate(sprint.getEndDate());
        dto.setStatus(sprint.getStatus());
        // For timeline, we might not need all tasks, but if needed, convert them here
        // dto.setTasks(sprint.getTasks().stream().map(this::convertToTaskDTO).collect(Collectors.toList()));
        return dto;
    }
}
