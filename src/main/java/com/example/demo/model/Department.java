package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "departments")
@Getter
@Setter
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private AppUser manager;
    
    @OneToMany(mappedBy = "department")
    private Set<AppUser> users;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
