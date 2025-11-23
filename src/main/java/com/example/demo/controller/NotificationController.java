package com.example.demo.controller;

import com.example.demo.model.Notification;
import com.example.demo.service.NotificationService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<Notification> getAllNotifications() {
        return notificationService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@PathVariable Long id) {
        Optional<Notification> notification = notificationService.findById(id);
        return notification.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Notification createNotification(@RequestBody Notification notification) {
        return notificationService.save(notification);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notification> updateNotification(@PathVariable Long id,
            @RequestBody Notification notificationDetails) {
        Optional<Notification> notificationOptional = notificationService.findById(id);
        if (notificationOptional.isPresent()) {
            Notification notification = notificationOptional.get();
            // Update fields here
            notification.setMessage(notificationDetails.getMessage());
            notification.setRead(notificationDetails.isRead());
            notification.setLinkToResource(notificationDetails.getLinkToResource());
            return ResponseEntity.ok(notificationService.save(notification));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        notificationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
