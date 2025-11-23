package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "projects")
@Getter
@Setter
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    private String priority;
    private String status;
    private LocalDate startDate;
    private LocalDate dueDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<ProjectMember> members;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Sprint> sprints;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private Set<Task> tasks;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
