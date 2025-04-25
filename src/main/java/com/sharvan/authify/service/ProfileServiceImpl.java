package com.sharvan.authify.service;

import com.sharvan.authify.entity.UserEntity;
import com.sharvan.authify.io.ProfileRequest;
import com.sharvan.authify.io.ProfileResponse;
import com.sharvan.authify.respository.UserRespository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
        String otp = String.valueOf(ThreadLocalRandom.current().nextInt(100000,1000000));

        //calculate expiry time
        Long expireTime = System.currentTimeMillis()+(1000*60*15);

        //update the profile
        existingUser.setResetOtp(otp);
        existingUser.setResetOtpExpireAt(expireTime);

        //save into the database
        userRespository.save(existingUser);
        emailService.sendResetOtpEmail(email,otp);
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
