package com.example.demo.service;

import com.example.demo.model.ActivityLog;
import com.example.demo.repository.ActivityLogRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public List<ActivityLog> findAll() {
        return activityLogRepository.findAll();
    }

    public Optional<ActivityLog> findById(Long id) {
        return activityLogRepository.findById(id);
    }

    public ActivityLog save(ActivityLog activityLog) {
        return activityLogRepository.save(activityLog);
    }

    public void deleteById(Long id) {
        activityLogRepository.deleteById(id);
    }
}
