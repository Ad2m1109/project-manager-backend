package com.example.demo.controller;

import com.example.demo.model.Attachment;
import com.example.demo.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    @Autowired
    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping
    public List<Attachment> getAllAttachments() {
        return attachmentService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Attachment> getAttachmentById(@PathVariable Long id) {
        Attachment attachment = attachmentService.findById(id);
        return ResponseEntity.ok(attachment);
    }

    @PostMapping
    public Attachment createAttachment(@RequestBody Attachment attachment) {
        return attachmentService.save(attachment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Attachment> updateAttachment(@PathVariable Long id, @RequestBody Attachment attachmentDetails) {
        Attachment attachment = attachmentService.findById(id);
        // Update fields here
        attachment.setFileName(attachmentDetails.getFileName());
        attachment.setFileUrl(attachmentDetails.getFileUrl());
        attachment.setFileType(attachmentDetails.getFileType());
        return ResponseEntity.ok(attachmentService.save(attachment));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable Long id) {
        attachmentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
