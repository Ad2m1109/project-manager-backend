package com.example.demo.controller;

import com.example.demo.dto.CommentDTO;
import com.example.demo.model.AppUser;
import com.example.demo.model.Comment;
import com.example.demo.model.Task;
import com.example.demo.service.AppUserService;
import com.example.demo.service.CommentService;
import com.example.demo.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final TaskService taskService;
    private final AppUserService appUserService;

    // Get all comments for a specific task
    @GetMapping("/tasks/{taskId}/comments")
    public ResponseEntity<List<CommentDTO>> getTaskComments(@PathVariable Long taskId) {
        List<Comment> comments = commentService.findByTaskId(taskId);
        List<CommentDTO> dtos = comments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // Create a new comment on a task
    @PostMapping("/tasks/{taskId}/comments")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Long taskId,
            @RequestBody CommentDTO commentDTO) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        AppUser currentUser = appUserService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task task = taskService.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setTask(task);
        comment.setUser(currentUser);

        Comment savedComment = commentService.save(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertToDTO(savedComment));
    }

    // Update a comment
    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentDTO> updateComment(
            @PathVariable Long id,
            @RequestBody CommentDTO commentDTO) {

        return commentService.findById(id)
                .map(existingComment -> {
                    existingComment.setContent(commentDTO.getContent());
                    return ResponseEntity.ok(convertToDTO(commentService.save(existingComment)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete a comment
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        if (commentService.findById(id).isPresent()) {
            commentService.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Helper method to convert Comment entity to CommentDTO
    private CommentDTO convertToDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());

        if (comment.getTask() != null) {
            dto.setTaskId(comment.getTask().getId());
            dto.setTaskTitle(comment.getTask().getTitle());
        }

        if (comment.getUser() != null) {
            dto.setUserId(comment.getUser().getId());
            dto.setUserName(comment.getUser().getFullName());
        }

        return dto;
    }
}
