package com.example.demo.service;

import com.example.demo.model.AIAnalysis;
import com.example.demo.repository.AIAnalysisRepository;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AIAnalysisService {

    private final AIAnalysisRepository aiAnalysisRepository;

    @Autowired
    public AIAnalysisService(AIAnalysisRepository aiAnalysisRepository) {
        this.aiAnalysisRepository = aiAnalysisRepository;
    }

    public List<AIAnalysis> findAll() {
        return aiAnalysisRepository.findAll();
    }

    public AIAnalysis findById(Long id) {
        return aiAnalysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AIAnalysis not found with id " + id));
    }

    public AIAnalysis save(AIAnalysis aiAnalysis) {
        return aiAnalysisRepository.save(aiAnalysis);
    }

    public void deleteById(Long id) {
        aiAnalysisRepository.deleteById(id);
    }
}
