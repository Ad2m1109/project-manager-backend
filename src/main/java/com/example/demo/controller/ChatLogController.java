package main.java.com.example.demo.controller;

import com.example.demo.model.AppUser;
import com.example.demo.model.ChatLog;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.ChatLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat-logs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatLogController {

    private final ChatLogRepository chatLogRepository;
    private final AppUserRepository appUserRepository;

    @GetMapping
    public ResponseEntity<List<ChatLog>> getChatHistory(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = appUserRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(chatLogRepository.findByUserIdOrderByTimestampAsc(user.getId()));
    }

    @PostMapping
    public ResponseEntity<ChatLog> saveChatMessage(@AuthenticationPrincipal UserDetails userDetails,
            @RequestBody Map<String, String> request) {
        AppUser user = appUserRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String message = request.get("message");
        String sender = request.get("sender");

        if (message == null || sender == null) {
            return ResponseEntity.badRequest().build();
        }

        ChatLog chatLog = ChatLog.builder()
                .user(user)
                .message(message)
                .sender(sender)
                .build();

        return ResponseEntity.ok(chatLogRepository.save(chatLog));
    }
}
