package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "ai_analysis")
@Getter
@Setter
public class AIAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "analysis_type", nullable = false)
    private String analysisType;

    @Lob
    @Column(name = "result_text", columnDefinition = "TEXT")
    private String resultText;

    @Column(updatable = false)
    private Instant createdAt = Instant.now();
}
