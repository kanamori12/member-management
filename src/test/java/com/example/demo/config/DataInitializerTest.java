package com.example.demo.config;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.CommandLineRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.demo.model.AppUser;
import com.example.demo.model.Role;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.service.AppUserService;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private AppUserService appUserService;

    private DataInitializer dataInitializer;

    @BeforeEach
    void setUp() {

        dataInitializer = new DataInitializer();

        ReflectionTestUtils.setField(
                dataInitializer,
                "initialAdminUsername",
                "admin");

        ReflectionTestUtils.setField(
                dataInitializer,
                "initialAdminPassword",
                "password");
    }

    @Test
    void initializeUser_初期ADMINが存在しなければ登録する() throws Exception {

        when(appUserRepository.findByUsername("admin"))
                .thenReturn(Optional.empty());

        CommandLineRunner runner =
                dataInitializer.initializeUser(
                        appUserRepository,
                        appUserService);

        runner.run();

        verify(appUserRepository)
                .findByUsername("admin");

        verify(appUserService)
                .register(
                        "admin",
                        "password",
                        Role.ADMIN);
    }

    @Test
    void initializeUser_初期ADMINが存在すれば登録しない() throws Exception {

        AppUser existingAdmin = new AppUser();
        existingAdmin.setUsername("admin");
        existingAdmin.setRole(Role.ADMIN);

        when(appUserRepository.findByUsername("admin"))
                .thenReturn(Optional.of(existingAdmin));

        CommandLineRunner runner =
                dataInitializer.initializeUser(
                        appUserRepository,
                        appUserService);

        runner.run();

        verify(appUserRepository)
                .findByUsername("admin");

        verify(appUserService, never())
                .register(
                        "admin",
                        "password",
                        Role.ADMIN);
    }
}