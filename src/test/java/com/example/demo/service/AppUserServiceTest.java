package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.exception.AppUserNotFoundException;
import com.example.demo.exception.UserOperationException;
import com.example.demo.model.AppUser;
import com.example.demo.model.AppUserEditForm;
import com.example.demo.model.AppUserForm;
import com.example.demo.model.AppUserPasswordForm;
import com.example.demo.model.Role;
import com.example.demo.repository.AppUserRepository;

@ExtendWith(MockitoExtension.class)
public class AppUserServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AppUserService appUserService;

    @BeforeEach
    void setUp() {

        appUserService = new AppUserService(
                appUserRepository,
                passwordEncoder);
    }

    @Test
    void register_パスワードをハッシュ化してユーザーを登録できる() {

        AppUserForm form = new AppUserForm();
        form.setUsername("testuser");
        form.setPassword("password123");
        form.setRole(Role.USER);

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");

        appUserService.register(form);

        ArgumentCaptor<AppUser> captor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(appUserRepository)
                .save(captor.capture());

        AppUser savedUser = captor.getValue();

        assertEquals(
                "testuser",
                savedUser.getUsername());

        assertEquals(
                "hashedPassword",
                savedUser.getPassword());

        assertEquals(
                Role.USER,
                savedUser.getRole());

        verify(passwordEncoder)
                .encode("password123");
    }

    @Test
    void register_同じユーザー名が存在する場合は例外になる() {

        AppUserForm form = new AppUserForm();
        form.setUsername("testuser");
        form.setPassword("password123");
        form.setRole(Role.USER);

        AppUser existingUser = new AppUser();
        existingUser.setUsername("testuser");

        when(appUserRepository.findByUsername("testuser"))
                .thenReturn(Optional.of(existingUser));

        assertThrows(
                UserOperationException.class,
                () -> appUserService.register(form));

        verify(appUserRepository, never())
                .save(any(AppUser.class));
    }

    @Test
    void delete_自分自身を削除しようとすると例外になる() {

        AppUser appUser = new AppUser();
        appUser.setId(1L);
        appUser.setUsername("admin");

        when(appUserRepository.findById(1L))
                .thenReturn(Optional.of(appUser));

        assertThrows(
                UserOperationException.class,
                () -> appUserService.delete(
                        1L,
                        "admin"));

        verify(appUserRepository, never())
                .delete(appUser);
    }

    @Test
    void update_自分自身のADMIN権限をUSERへ変更しようとすると例外になる() {

        AppUser appUser = new AppUser();
        appUser.setId(1L);
        appUser.setUsername("admin");
        appUser.setRole(Role.ADMIN);

        AppUserEditForm form = new AppUserEditForm();
        form.setId(1L);
        form.setUsername("admin");
        form.setRole(Role.USER);

        when(appUserRepository.findById(1L))
                .thenReturn(Optional.of(appUser));

        assertThrows(
                UserOperationException.class,
                () -> appUserService.update(
                        form,
                        "admin"));

        verify(appUserRepository, never())
                .save(appUser);
    }

    @Test
    void update_他ユーザーの権限を変更できる() {

        AppUser appUser = new AppUser();
        appUser.setId(2L);
        appUser.setUsername("user");
        appUser.setRole(Role.USER);

        AppUserEditForm form = new AppUserEditForm();
        form.setId(2L);
        form.setUsername("user");
        form.setRole(Role.ADMIN);

        when(appUserRepository.findById(2L))
                .thenReturn(Optional.of(appUser));

        appUserService.update(
                form,
                "admin");

        assertEquals(
                "user",
                appUser.getUsername());

        assertEquals(
                Role.ADMIN,
                appUser.getRole());

        verify(appUserRepository)
                .save(appUser);
    }

    @Test
    void updatePassword_パスワードをハッシュ化して更新できる() {

        AppUser appUser = new AppUser();
        appUser.setId(2L);
        appUser.setUsername("user");
        appUser.setPassword("oldPassword");

        AppUserPasswordForm form =
                new AppUserPasswordForm();

        form.setId(2L);
        form.setPassword("newPassword");
        form.setConfirmPassword("newPassword");

        when(appUserRepository.findById(2L))
                .thenReturn(Optional.of(appUser));

        when(passwordEncoder.encode("newPassword"))
                .thenReturn("hashedNewPassword");

        appUserService.updatePassword(form);

        assertEquals(
                "hashedNewPassword",
                appUser.getPassword());

        verify(passwordEncoder)
                .encode("newPassword");

        verify(appUserRepository)
                .save(appUser);
    }

    @Test
    void findById_存在しないユーザーIDなら例外になる() {

        when(appUserRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                AppUserNotFoundException.class,
                () -> appUserService.findById(999L));
    }
}