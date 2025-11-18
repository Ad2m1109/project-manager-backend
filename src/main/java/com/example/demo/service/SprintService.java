package com.example.demo.service;

import com.example.demo.model.Sprint;
import com.example.demo.repository.SprintRepository;
import com.example.demo.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;

    @Autowired
    public SprintService(SprintRepository sprintRepository) {
        this.sprintRepository = sprintRepository;
    }

    public List<Sprint> findAll() {
        return sprintRepository.findAll();
    }

    public Sprint findById(Long id) {
        return sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found with id " + id));
    }

    public Sprint save(Sprint sprint) {
        return sprintRepository.save(sprint);
    }

    public void deleteById(Long id) {
        sprintRepository.deleteById(id);
    }
}
