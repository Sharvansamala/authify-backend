package com.sharvan.authify.service;

import com.sharvan.authify.entity.UserEntity;
import com.sharvan.authify.respository.UserRespository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserRespository userRespository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRespository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found for email " + email));
        return new User(user.getEmail(), user.getPassword(), new ArrayList<>());
    }
}

