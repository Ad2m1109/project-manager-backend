package com.example.demo.service;

import com.example.demo.model.Sprint;
import com.example.demo.repository.SprintRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;

    public SprintService(SprintRepository sprintRepository) {
        this.sprintRepository = sprintRepository;
    }

    public List<Sprint> findAll() {
        return sprintRepository.findAll();
    }

    public List<Sprint> findByProjectId(Long projectId) {
        return sprintRepository.findByProjectIdWithTasks(projectId);
    }

    public Optional<Sprint> findById(Long id) {
        return sprintRepository.findById(id);
    }

    public Sprint save(Sprint sprint) {
        return sprintRepository.save(sprint);
    }

    public void deleteById(Long id) {
        sprintRepository.deleteById(id);
    }
}
