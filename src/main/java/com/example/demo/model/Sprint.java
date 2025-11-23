package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "sprints")
@Getter
@Setter
public class Sprint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    private String goal;

    private LocalDate startDate;
    private LocalDate endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @OneToMany(mappedBy = "sprint")
    private Set<Task> tasks;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
