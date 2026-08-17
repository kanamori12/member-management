package com.example.demo.config;


import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.repository.AppUserRepository;
import com.example.demo.service.AppUserService;
import com.example.demo.model.Role;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeUser(
            AppUserRepository appUserRepository,
            AppUserService appUserService) {

        return args -> {

            if (appUserRepository.findByUsername("admin").isEmpty()) {
                appUserService.register(
                        "admin",
                        "password",
                        Role.ADMIN);
            }

            if (appUserRepository.findByUsername("user").isEmpty()) {
                appUserService.register(
                        "user",
                        "password",
                        Role.USER);
            }
        };
    }
}