package com.sharvan.authify.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendWelcomeEmail(String toEmail, String name) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject("Welcome to Authify");
        mailMessage.setText("Hello " + name + "\n\nThanks for registering with us!\n\nRegards,\n Sharvan");
        javaMailSender.send(mailMessage);
    }

    public void sendResetOtpEmail(String toEmail, String otp) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject("Password Reset OTP");
        mailMessage.setText("Your OTP for reset: " + otp + ". Expires in 15 minutes");
        javaMailSender.send(mailMessage);
    }

    public void sendPasswordUpdateEmail(String toEmail, String name) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(toEmail);
        mailMessage.setSubject("Password Updated");
        mailMessage.setText("Hello " + name + ",\n\nYour Password has updated successfully.\n\nclick here to login.\n\nRegards,\nTeam.");
        javaMailSender.send(mailMessage);
    }

    public void sendVerifyOtp(String email, String verifyOtp, String name) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(fromEmail);
        mailMessage.setTo(email);
        mailMessage.setSubject("Verify your email");
        mailMessage.setText(String.format("""
                Hello %s,
                Your 6-digit otp to verify your account is: %s
                Regards,
                Team
                """, name, verifyOtp));
        javaMailSender.send(mailMessage);
    }

}
