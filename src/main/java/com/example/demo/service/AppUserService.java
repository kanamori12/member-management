package com.example.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.model.AppUser;
import com.example.demo.repository.AppUserRepository;

@Service
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserService(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder) {

        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(
            String username,
            String rawPassword,
            String role) {

        AppUser appUser = new AppUser();

        appUser.setUsername(username);

        appUser.setPassword(
                passwordEncoder.encode(rawPassword)
        );

        appUser.setRole(role);

        appUserRepository.save(appUser);
    }
}