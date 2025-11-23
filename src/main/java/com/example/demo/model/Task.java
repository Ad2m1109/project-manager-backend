package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "tasks")
@Getter
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String description;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String priority;

    private Instant dueDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id", nullable = false)
    @JsonIgnoreProperties({ "tasks", "sprints", "members", "company" })
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sprint_id")
    @JsonIgnoreProperties({ "tasks", "project" })
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignee_id")
    @JsonIgnoreProperties({ "projectMemberships", "company", "department" })
    private AppUser assignee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter_id")
    @JsonIgnoreProperties({ "projectMemberships", "company", "department" })
    private AppUser reporter;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_task_id")
    @JsonIgnoreProperties({ "subTasks", "comments", "project", "sprint", "assignee", "reporter", "parentTask" })
    private Task parentTask;

    @OneToMany(mappedBy = "parentTask")
    @JsonIgnore
    private Set<Task> subTasks;

    @OneToMany(mappedBy = "task")
    @JsonIgnore
    private Set<Comment> comments;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
