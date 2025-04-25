package com.sharvan.authify.controller;

import com.sharvan.authify.io.ProfileRequest;
import com.sharvan.authify.io.ProfileResponse;
import com.sharvan.authify.service.EmailService;
import com.sharvan.authify.service.ProfileService;
import com.sharvan.authify.service.ProfileServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileServiceImpl profileService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<ProfileResponse> createProfile(@Valid @RequestBody ProfileRequest request){
        ProfileResponse profileResponse= profileService.createProfile(request);
        emailService.sendWelcomeEmail(request.getEmail(),request.getName());
        return new ResponseEntity<>(profileResponse, HttpStatus.CREATED);
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(@CurrentSecurityContext(expression = "authentication?.name") String email){
        ProfileResponse profileResponse = profileService.getProfile(email);
        return ResponseEntity.ok(profileResponse);
    }
}
