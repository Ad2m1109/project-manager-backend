package com.example.demo.service;

import com.example.demo.model.ProjectMember;
import com.example.demo.model.ProjectMemberId;
import com.example.demo.repository.ProjectMemberRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectMemberService {

    private final ProjectMemberRepository projectMemberRepository;

    public ProjectMemberService(ProjectMemberRepository projectMemberRepository) {
        this.projectMemberRepository = projectMemberRepository;
    }

    public List<ProjectMember> findAll() {
        return projectMemberRepository.findAll();
    }

    public List<ProjectMember> findByProjectId(Long projectId) {
        return projectMemberRepository.findByIdProjectId(projectId);
    }

    public Optional<ProjectMember> findById(ProjectMemberId id) {
        return projectMemberRepository.findById(id);
    }

    public ProjectMember save(ProjectMember projectMember) {
        return projectMemberRepository.save(projectMember);
    }

    public void deleteById(ProjectMemberId id) {
        projectMemberRepository.deleteById(id);
    }
}
