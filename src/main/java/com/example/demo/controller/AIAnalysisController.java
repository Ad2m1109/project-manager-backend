package com.example.demo.controller;

import com.example.demo.model.AIAnalysis;
import com.example.demo.service.AIAnalysisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ai-analyses")
public class AIAnalysisController {

    private final AIAnalysisService aiAnalysisService;

    @Autowired
    public AIAnalysisController(AIAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @GetMapping
    public List<AIAnalysis> getAllAIAnalyses() {
        return aiAnalysisService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AIAnalysis> getAIAnalysisById(@PathVariable Long id) {
        AIAnalysis aiAnalysis = aiAnalysisService.findById(id);
        return ResponseEntity.ok(aiAnalysis);
    }

    @PostMapping
    public AIAnalysis createAIAnalysis(@RequestBody AIAnalysis aiAnalysis) {
        return aiAnalysisService.save(aiAnalysis);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AIAnalysis> updateAIAnalysis(@PathVariable Long id, @RequestBody AIAnalysis aiAnalysisDetails) {
        AIAnalysis aiAnalysis = aiAnalysisService.findById(id);
        // Update fields here
        // aiAnalysis.set...
        return ResponseEntity.ok(aiAnalysisService.save(aiAnalysis));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAIAnalysis(@PathVariable Long id) {
        aiAnalysisService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
