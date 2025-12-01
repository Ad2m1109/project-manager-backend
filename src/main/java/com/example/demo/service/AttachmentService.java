package com.example.demo.service;

import com.example.demo.model.Attachment;
import com.example.demo.repository.AttachmentRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AttachmentService {

    private final AttachmentRepository attachmentRepository;

    public AttachmentService(AttachmentRepository attachmentRepository) {
        this.attachmentRepository = attachmentRepository;
    }

    public List<Attachment> findAll() {
        return attachmentRepository.findAll();
    }

    public List<Attachment> findByTaskId(Long taskId) {
        return attachmentRepository.findByTaskId(taskId);
    }

    public Optional<Attachment> findById(Long id) {
        return attachmentRepository.findById(id);
    }

    public Attachment save(Attachment attachment) {
        return attachmentRepository.save(attachment);
    }

    public void deleteById(Long id) {
        attachmentRepository.deleteById(id);
    }
}
