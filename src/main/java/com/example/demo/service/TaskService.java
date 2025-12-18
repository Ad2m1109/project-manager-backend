package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Task;
import com.example.demo.repository.TaskRepository;

import com.example.demo.repository.TaskSpecification;
import org.springframework.data.jpa.domain.Specification;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<Task> findAll(Specification<Task> spec) {
        // Ensure assignee, project, and sprint are fetched for filtered tasks
        return taskRepository.findAll(spec); // JpaSpecificationExecutor doesn't directly support JOIN FETCH in findAll,
                                            // but the specification itself can define joins.
                                            // For now, we rely on the default behavior or ensure the spec includes fetches.
                                            // If performance is an issue, a custom method in repo would be needed.
    }

    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Task> findByProjectId(Long projectId) {
        return taskRepository.findByProjectIdWithDetails(projectId);
    }

    @Transactional(readOnly = true)
    public List<Task> findByProjectIdAndAssigneeId(Long projectId, Long assigneeId) {
        return taskRepository.findByProjectIdAndAssigneeIdWithDetails(projectId, assigneeId);
    }

    @Transactional(readOnly = true)
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    public Task save(Task task) {
        return taskRepository.save(task);
    }

    public void deleteById(Long id) {
        taskRepository.deleteById(id);
    }
}
