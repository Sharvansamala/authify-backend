package com.sharvan.authify.service;

import com.sharvan.authify.entity.UserEntity;
import com.sharvan.authify.exception.BadRequestException;
import com.sharvan.authify.io.ProfileRequest;
import com.sharvan.authify.io.ProfileResponse;
import com.sharvan.authify.io.ResetPasswordResponse;
import com.sharvan.authify.respository.UserRespository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {
    //    private final ModelMapper modelMapper;
    private final UserRespository userRespository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Override
    public ProfileResponse createProfile(ProfileRequest request) {
        log.info("Creating profile");
        if (userRespository.existsByEmail(request.getEmail()))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        UserEntity entity = convertToUserEntity(request);
        entity = userRespository.save(entity);
        return convertToProfileResponse(entity);
    }

    @Override
    public ProfileResponse getProfile(String email) {
        UserEntity entity = userRespository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        return convertToProfileResponse(entity);
    }

    @Override
    public void sendResetOtp(String email) {
        UserEntity existingUser = userRespository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        //Generate 6 digit otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        //calculate expiry time
        Long expireTime = System.currentTimeMillis() + (1000 * 60 * 15);

        //update the profile
        existingUser.setResetOtp(otp);
        existingUser.setResetOtpExpireAt(expireTime);

        //save into the database
        userRespository.save(existingUser);
        emailService.sendResetOtpEmail(email, otp);
    }

    @Override
    public ResetPasswordResponse resetPassword(String email, String otp, String newPassword) {
        UserEntity existingUser = userRespository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        String oldPassword = existingUser.getPassword();
        if (existingUser.getResetOtp() == null || !existingUser.getResetOtp().equals(otp)) {
            throw new RuntimeException("Invalid Otp");
        } else if (existingUser.getResetOtpExpireAt() < System.currentTimeMillis())
            throw new RuntimeException("Otp Expired");
        existingUser.setPassword(passwordEncoder.encode(newPassword));
        existingUser.setResetOtp(null);
        existingUser.setResetOtpExpireAt(0L);
        userRespository.save(existingUser);
        String newPass = existingUser.getPassword();
        if (!oldPassword.equals(newPass)) {
            emailService.sendPasswordUpdateEmail(existingUser.getEmail(), existingUser.getName());
            return new ResetPasswordResponse("Password Updated");
        }
        return new ResetPasswordResponse("Password Not Updated");
    }

    @Override
    public void allEmail() {
        List<String> emails = userRespository.findAllEmails();
        emails.forEach(mail -> emailService.sendWelcomeEmail(mail, "XO"));
    }

    @Override
    public void sendOtp(String email) {
        UserEntity existingUser = userRespository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        if (existingUser.getIsAccountVerified() != null && existingUser.getIsAccountVerified()) return;
        //Generate 6 digit otp
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        //calculate expiry time
        Long expireTime = System.currentTimeMillis() + (1000 * 60 * 60 * 24);

        //update the profile
        existingUser.setVerifyOtp(otp);
        existingUser.setVerifyOtpExpireAt(expireTime);

        //save to database
        userRespository.save(existingUser);
        emailService.sendVerifyOtp(existingUser.getEmail(), existingUser.getVerifyOtp(), existingUser.getName());
    }

    @Override
    public void verifyOtp(String email, String otp) {
        UserEntity existingUser = userRespository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));
        if (existingUser.getIsAccountVerified() != null && existingUser.getIsAccountVerified()) return;

        if (existingUser.getVerifyOtp() == null||!existingUser.getVerifyOtp().equals(otp))
            throw new BadRequestException("Invalid OTP");
        else if (existingUser.getVerifyOtpExpireAt() < System.currentTimeMillis())
            throw new BadRequestException("OTP expired");

        existingUser.setIsAccountVerified(true);
        existingUser.setVerifyOtp(null);
        existingUser.setVerifyOtpExpireAt(0L);
        userRespository.save(existingUser);
    }


    private ProfileResponse convertToProfileResponse(UserEntity entity) {
        return ProfileResponse.builder()
                .userId(entity.getUserId())
                .email(entity.getEmail())
                .name(entity.getName())
                .isAccountVerified(entity.getIsAccountVerified())
                .build();
    }

    private UserEntity convertToUserEntity(ProfileRequest request) {
        return UserEntity.builder()
                .email(request.getEmail())
                .userId(UUID.randomUUID().toString())
                .name(request.getName())
                .password(passwordEncoder.encode(request.getPassword()))
                .isAccountVerified(false)
                .resetOtpExpireAt(0L)
                .verifyOtp(null)
                .verifyOtpExpireAt(0L)
                .resetOtp(null)
                .build();
    }
}
