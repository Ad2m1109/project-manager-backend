package com.example.demo.service;

import com.example.demo.model.VerificationCode;
import com.example.demo.repository.VerificationCodeRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final VerificationCodeRepository verificationCodeRepository;

    @Transactional
    public void sendVerificationCode(String email) throws MessagingException {
        String code = generateCode();

        // Save or update code in DB
        VerificationCode verificationCode = verificationCodeRepository.findByEmail(email)
                .orElse(new VerificationCode());

        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setExpiryDate(Instant.now().plus(10, ChronoUnit.MINUTES));

        verificationCodeRepository.save(verificationCode);

        // Send email
        sendEmail(email, code);
    }

    private String generateCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    private void sendEmail(String to, String code) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Your IntelliManage Verification Code");

        String content = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px;'>"
                +
                "<h2 style='color: #4A90E2; text-align: center;'>IntelliManage</h2>" +
                "<p>Hello,</p>" +
                "<p>Thank you for joining IntelliManage! Please use the following 6-digit code to verify your email address:</p>"
                +
                "<div style='background-color: #f9f9f9; padding: 15px; border-radius: 5px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #333;'>"
                +
                code +
                "</div>" +
                "<p>This code will expire in 10 minutes.</p>" +
                "<p>If you did not request this code, please ignore this email.</p>" +
                "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>" +
                "<p style='font-size: 12px; color: #888; text-align: center;'>&copy; 2025 IntelliManage. All rights reserved.</p>"
                +
                "</div>";

        helper.setText(content, true);
        mailSender.send(message);
    }
}
