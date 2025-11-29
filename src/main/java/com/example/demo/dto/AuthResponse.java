package com.example.demo.dto;

import com.example.demo.model.AppUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private AppUser user;
    private String message;
}
