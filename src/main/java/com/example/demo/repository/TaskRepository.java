package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.demo.model.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.project LEFT JOIN FETCH t.sprint WHERE t.project.id = :projectId")
    List<Task> findByProjectIdWithDetails(Long projectId);
    
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.assignee LEFT JOIN FETCH t.project LEFT JOIN FETCH t.sprint WHERE t.project.id = :projectId AND t.assignee.id = :assigneeId")
    List<Task> findByProjectIdAndAssigneeIdWithDetails(Long projectId, Long assigneeId);

    // Keep original methods for compatibility if needed elsewhere, or remove if only WithDetails are used
    List<Task> findByProjectId(Long projectId);
    
    List<Task> findByProjectIdAndAssigneeId(Long projectId, Long assigneeId);
}
