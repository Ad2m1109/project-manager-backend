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
        Optional<AIAnalysis> aiAnalysis = aiAnalysisService.findById(id);
        return aiAnalysis.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public AIAnalysis createAIAnalysis(@RequestBody AIAnalysis aiAnalysis) {
        return aiAnalysisService.save(aiAnalysis);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AIAnalysis> updateAIAnalysis(@PathVariable Long id, @RequestBody AIAnalysis aiAnalysisDetails) {
        Optional<AIAnalysis> aiAnalysisOptional = aiAnalysisService.findById(id);
        if (aiAnalysisOptional.isPresent()) {
            AIAnalysis aiAnalysis = aiAnalysisOptional.get();
            // Update fields here
            // aiAnalysis.set...
            return ResponseEntity.ok(aiAnalysisService.save(aiAnalysis));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAIAnalysis(@PathVariable Long id) {
        aiAnalysisService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
