package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.demo.model.AppUser;
import com.example.demo.model.Role;
import com.example.demo.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
public class CustomUserDetailsServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() {

        customUserDetailsService =
                new CustomUserDetailsService(
                        appUserRepository);
    }

    @Test
    void loadUserByUsername_存在するユーザーをUserDetailsとして取得できる() {

        AppUser appUser = new AppUser();

        appUser.setId(1L);
        appUser.setUsername("admin");
        appUser.setPassword("hashedPassword");
        appUser.setRole(Role.ADMIN);

        when(appUserRepository.findByUsername("admin"))
                .thenReturn(Optional.of(appUser));

        UserDetails result =
                customUserDetailsService
                        .loadUserByUsername("admin");

        assertEquals(
                "admin",
                result.getUsername());

        assertEquals(
                "hashedPassword",
                result.getPassword());

        assertEquals(
                1,
                result.getAuthorities().size());

        assertEquals(
                "ROLE_ADMIN",
                result.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority());

        verify(appUserRepository)
                .findByUsername("admin");
    }

    @Test
    void loadUserByUsername_USERならROLE_USERになる() {

        AppUser appUser = new AppUser();

        appUser.setId(2L);
        appUser.setUsername("user");
        appUser.setPassword("hashedPassword");
        appUser.setRole(Role.USER);

        when(appUserRepository.findByUsername("user"))
                .thenReturn(Optional.of(appUser));

        UserDetails result =
                customUserDetailsService
                        .loadUserByUsername("user");

        assertEquals(
                "user",
                result.getUsername());

        assertEquals(
                "ROLE_USER",
                result.getAuthorities()
                        .iterator()
                        .next()
                        .getAuthority());

        verify(appUserRepository)
                .findByUsername("user");
    }

    @Test
    void loadUserByUsername_存在しないユーザーなら例外になる() {

        when(appUserRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        UsernameNotFoundException exception =
                assertThrows(
                        UsernameNotFoundException.class,
                        () -> customUserDetailsService
                                .loadUserByUsername("unknown"));

        assertEquals(
                "ユーザーが見つかりません: unknown",
                exception.getMessage());

        verify(appUserRepository)
                .findByUsername("unknown");
    }
}