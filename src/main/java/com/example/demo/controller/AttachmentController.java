package com.example.demo.controller;

import com.example.demo.model.Attachment;
import com.example.demo.service.AttachmentService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public List<Attachment> getAllAttachments() {
        return attachmentService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Attachment> getAttachmentById(@PathVariable Long id) {
        Optional<Attachment> attachment = attachmentService.findById(id);
        return attachment.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Attachment createAttachment(@RequestBody Attachment attachment) {
        return attachmentService.save(attachment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Attachment> updateAttachment(@PathVariable Long id,
            @RequestBody Attachment attachmentDetails) {
        Optional<Attachment> attachmentOptional = attachmentService.findById(id);
        if (attachmentOptional.isPresent()) {
            Attachment attachment = attachmentOptional.get();
            // Update fields here
            attachment.setFileName(attachmentDetails.getFileName());
            attachment.setFileUrl(attachmentDetails.getFileUrl());
            attachment.setFileType(attachmentDetails.getFileType());
            return ResponseEntity.ok(attachmentService.save(attachment));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
