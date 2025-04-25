package com.sharvan.authify.service;

import com.sharvan.authify.io.ProfileRequest;
import com.sharvan.authify.io.ProfileResponse;

public interface ProfileService {
    ProfileResponse createProfile(ProfileRequest request);

    ProfileResponse getProfile(String email);

    void sendResetOtp(String email);
}
