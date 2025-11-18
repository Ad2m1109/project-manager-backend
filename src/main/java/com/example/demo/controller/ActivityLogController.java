package com.example.demo.controller;

import com.example.demo.model.ActivityLog;
import com.example.demo.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/activity-logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    @Autowired
    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public List<ActivityLog> getAllActivityLogs() {
        return activityLogService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityLog> getActivityLogById(@PathVariable Long id) {
        ActivityLog activityLog = activityLogService.findById(id);
        return ResponseEntity.ok(activityLog);
    }

    @PostMapping
    public ActivityLog createActivityLog(@RequestBody ActivityLog activityLog) {
        return activityLogService.save(activityLog);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityLog> updateActivityLog(@PathVariable Long id, @RequestBody ActivityLog activityLogDetails) {
        ActivityLog activityLog = activityLogService.findById(id);
        // Update fields here
        // activityLog.set...
        return ResponseEntity.ok(activityLogService.save(activityLog));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivityLog(@PathVariable Long id) {
        activityLogService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
