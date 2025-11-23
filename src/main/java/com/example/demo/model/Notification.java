package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false)
    private String message;

    private boolean isRead = false;

    private String linkToResource;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();
}
