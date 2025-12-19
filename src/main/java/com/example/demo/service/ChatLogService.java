package com.example.demo.service;

import com.example.demo.model.ChatLog;
import com.example.demo.repository.ChatLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatLogService {

    private final ChatLogRepository chatLogRepository;

    public ChatLog saveChatLog(ChatLog chatLog) {
        return chatLogRepository.save(chatLog);
    }
}
