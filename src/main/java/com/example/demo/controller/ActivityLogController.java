package com.example.demo.controller;

import com.example.demo.model.ActivityLog;
import com.example.demo.service.ActivityLogService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/activity-logs")
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    public ActivityLogController(ActivityLogService activityLogService) {
        this.activityLogService = activityLogService;
    }

    @GetMapping
    public List<ActivityLog> getAllActivityLogs() {
        return activityLogService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ActivityLog> getActivityLogById(@PathVariable Long id) {
        Optional<ActivityLog> activityLog = activityLogService.findById(id);
        return activityLog.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ActivityLog createActivityLog(@RequestBody ActivityLog activityLog) {
        return activityLogService.save(activityLog);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActivityLog> updateActivityLog(@PathVariable Long id,
            @RequestBody ActivityLog activityLogDetails) {
        Optional<ActivityLog> activityLogOptional = activityLogService.findById(id);
        if (activityLogOptional.isPresent()) {
            ActivityLog activityLog = activityLogOptional.get();
            return ResponseEntity.ok(activityLogService.save(activityLog));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteActivityLog(@PathVariable Long id) {
        activityLogService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
