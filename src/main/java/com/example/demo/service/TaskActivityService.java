package com.example.demo.service;

import com.example.demo.model.TaskActivity;
import com.example.demo.repository.TaskActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskActivityService {
    private final TaskActivityRepository taskActivityRepository;

    @Transactional
    public TaskActivity save(TaskActivity activity) {
        return taskActivityRepository.save(activity);
    }

    @Transactional(readOnly = true)
    public List<TaskActivity> findByTaskId(Long taskId) {
        return taskActivityRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }
}
