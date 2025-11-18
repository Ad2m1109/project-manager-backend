package com.example.demo.service;

import com.example.demo.model.ProjectMember;
import com.example.demo.repository.ProjectMemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;

    @Autowired
    public ProjectMemberService(ProjectMemberRepository projectMemberRepository) {
        this.projectMemberRepository = projectMemberRepository;
    }

    public List<ProjectMember> findAll() {
        return projectMemberRepository.findAll();
    }

    public Optional<ProjectMember> findById(Long id) {
        return projectMemberRepository.findById(id);
    }

    public ProjectMember save(ProjectMember projectMember) {
        return projectMemberRepository.save(projectMember);
    }

    public void deleteById(Long id) {
        projectMemberRepository.deleteById(id);
    }
}
