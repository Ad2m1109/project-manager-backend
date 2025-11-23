package com.example.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "companies")
@Getter
@Setter
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private Set<Department> departments;

    @OneToMany(mappedBy = "company")
    @JsonIgnore
    private Set<AppUser> users;

    @OneToMany(mappedBy = "company")
    private Set<Project> projects;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
