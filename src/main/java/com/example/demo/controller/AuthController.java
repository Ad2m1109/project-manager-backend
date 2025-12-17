package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.AppUser;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.AppUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Map;
import com.example.demo.service.EmailService;
import com.example.demo.repository.VerificationCodeRepository;
import com.example.demo.model.VerificationCode;
import com.example.demo.service.GoogleAuthService;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AppUserService appUserService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final GoogleAuthService googleAuthService;
    private final EmailService emailService;
    private final VerificationCodeRepository verificationCodeRepository;

    @Transactional
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String code = request.get("code");

        if (email == null || code == null) {
            return ResponseEntity.badRequest().body("Email and code are required");
        }

        try {
            Optional<VerificationCode> verificationCodeOpt = verificationCodeRepository.findByEmail(email);

            if (verificationCodeOpt.isPresent()) {
                VerificationCode verificationCode = verificationCodeOpt.get();
                if (verificationCode.isExpired()) {
                    return ResponseEntity.status(HttpStatus.GONE).body("Verification code has expired");
                }
                if (verificationCode.getCode().equals(code)) {
                    verificationCodeRepository.deleteByEmail(email);

                    // Activate user
                    appUserService.findByEmail(email).ifPresent(user -> {
                        user.setEnabled(true);
                        appUserService.save(user);
                    });

                    return ResponseEntity.ok().body(Map.of("message", "Email verified successfully"));
                } else {
                    return ResponseEntity.badRequest().body("Invalid verification code");
                }
            } else {
                return ResponseEntity.badRequest().body("No verification code found for this email");
            }
        } catch (Exception e) {
            log.error("Error during email verification for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Verification failed: " + e.getMessage());
        }
    }

    @PostMapping("/resend-code")
    public ResponseEntity<?> resendCode(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body("Email is required");
        }

        try {
            emailService.sendVerificationCode(email);
            return ResponseEntity.ok().body(Map.of("message", "Verification code resent successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send verification code: " + e.getMessage());
        }
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleLogin(@RequestBody java.util.Map<String, String> request) {
        String idToken = request.get("idToken");
        if (idToken == null) {
            return ResponseEntity.badRequest().body("ID Token is required");
        }

        try {
            com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload payload = googleAuthService
                    .verifyToken(idToken);
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            Optional<AppUser> userOptional = appUserService.findByEmail(email);
            AppUser user;

            if (userOptional.isPresent()) {
                user = userOptional.get();
            } else {
                // Register new user
                user = new AppUser();
                user.setEmail(email);
                user.setFullName(name != null ? name : email);
                user.setRoleType("EMPLOYEE"); // Default role
                user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString())); // Random password for
                                                                                                  // OAuth users
                user.setEnabled(true); // Google users are already verified
                user = appUserService.save(user);
            }

            String token = jwtUtil.generateToken(user);
            return ResponseEntity.ok(new AuthResponse(token, user, "Google login successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Google authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            // Check if email already exists
            if (appUserService.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Email already in use");
            }

            // Create new user
            AppUser newUser = new AppUser();
            newUser.setFullName(request.getFullName());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(passwordEncoder.encode(request.getPassword())); // Encoded password
            newUser.setRoleType(request.getRoleType());

            AppUser savedUser = appUserService.save(newUser);

            // Send verification code
            emailService.sendVerificationCode(savedUser.getEmail());

            return ResponseEntity
                    .ok(new AuthResponse(null, savedUser, "User registered successfully. Please verify your email."));
        } catch (Exception e) {
            log.error("Registration failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()));

            // If authentication successful, generate token
            AppUser user = appUserService.findByEmail(request.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String token = jwtUtil.generateToken(user);

            return ResponseEntity.ok(new AuthResponse(token, user, "Login successful"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No token provided");
        }

        try {
            String jwt = token.substring(7);
            String email = jwtUtil.extractUsername(jwt);

            Optional<AppUser> user = appUserService.findByEmail(email);
            if (user.isPresent()) {
                return ResponseEntity.ok(user.get());
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        // In a real application with JWT, you might want to blacklist the token
        return ResponseEntity.ok().body("Logout successful");
    }
}
