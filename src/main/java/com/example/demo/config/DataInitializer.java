package com.example.demo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.model.Role;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.service.AppUserService;

@Configuration
public class DataInitializer {

    @Value("${app.initial-admin.username}")
    private String initialAdminUsername;

    @Value("${app.initial-admin.password}")
    private String initialAdminPassword;

    @Bean
    public CommandLineRunner initializeUser(
            AppUserRepository appUserRepository,
            AppUserService appUserService) {

        return args -> {

            // 初期ADMINが存在しない場合だけ作成
            if (appUserRepository
                    .findByUsername(initialAdminUsername)
                    .isEmpty()) {

                appUserService.register(
                        initialAdminUsername,
                        initialAdminPassword,
                        Role.ADMIN);
            }
        };
    }
}