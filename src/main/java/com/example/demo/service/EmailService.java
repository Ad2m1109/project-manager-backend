package com.example.demo.service;

import com.example.demo.model.VerificationCode;
import com.example.demo.repository.VerificationCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VerificationCodeRepository verificationCodeRepository;

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ademyoussfi57@gmail.com"); // This should ideally be configured in application.properties
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void sendVerificationCode(String email) {
        String code = generateCode();
        saveVerificationCode(email, code, "VERIFICATION");
        String subject = "Email Verification Code";
        String text = "Your verification code is: " + code + "\n\n" 
                    + "This code is valid for 10 minutes.";
        sendSimpleEmail(email, subject, text);
    }

    public void sendPasswordResetCode(String email) {
        String code = generateCode();
        saveVerificationCode(email, code, "RESET");
        String subject = "Password Reset Code";
        String text = "Your password reset code is: " + code + "\n\n" 
                    + "This code is valid for 10 minutes.";
        sendSimpleEmail(email, subject, text);
    }

    private String generateCode() {
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    private void saveVerificationCode(String email, String code, String type) {
        verificationCodeRepository.deleteByEmailAndType(email, type); // Delete existing codes for this email and type

        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setType(type);
        verificationCode.setExpiryDate(Instant.now().plusSeconds(600)); // 10 minutes expiry
        verificationCodeRepository.save(verificationCode);
    }
}
