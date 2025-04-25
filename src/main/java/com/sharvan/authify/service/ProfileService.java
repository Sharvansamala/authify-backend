package com.sharvan.authify.service;

import com.sharvan.authify.io.ProfileRequest;
import com.sharvan.authify.io.ProfileResponse;
import com.sharvan.authify.io.ResetPasswordResponse;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest request);

    ProfileResponse getProfile(String email);

    void sendResetOtp(String email);

    ResetPasswordResponse resetPassword(String email, String otp, String newPassword);

    void allEmail();

    void sendOtp(String email);

    void verifyOtp(String email,String otp);
}
