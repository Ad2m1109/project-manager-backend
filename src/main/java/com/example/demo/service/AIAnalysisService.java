package com.example.demo.service;

import com.example.demo.model.AIAnalysis;
import com.example.demo.repository.AIAnalysisRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AIAnalysisService {

    private final AIAnalysisRepository aiAnalysisRepository;

    public AIAnalysisService(AIAnalysisRepository aiAnalysisRepository) {
        this.aiAnalysisRepository = aiAnalysisRepository;
    }

    public List<AIAnalysis> findAll() {
        return aiAnalysisRepository.findAll();
    }

    public Optional<AIAnalysis> findById(Long id) {
        return aiAnalysisRepository.findById(id);
    }

    public AIAnalysis save(AIAnalysis aiAnalysis) {
        return aiAnalysisRepository.save(aiAnalysis);
    }

    public void deleteById(Long id) {
        aiAnalysisRepository.deleteById(id);
    }
}
