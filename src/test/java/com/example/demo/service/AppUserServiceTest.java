package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
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


    // =========================
    // register
    // =========================

    @Test
    void register_初期ユーザーをハッシュ化して登録できる() {

        when(passwordEncoder.encode("password"))
                .thenReturn("hashedPassword");

        appUserService.register(
                "admin",
                "password",
                Role.ADMIN);

        ArgumentCaptor<AppUser> captor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(appUserRepository)
                .save(captor.capture());

        AppUser savedUser = captor.getValue();

        assertEquals(
                "admin",
                savedUser.getUsername());

        assertEquals(
                "hashedPassword",
                savedUser.getPassword());

        assertEquals(
                Role.ADMIN,
                savedUser.getRole());

        verify(passwordEncoder)
                .encode("password");
    }

    @Test
    void register_フォームからパスワードをハッシュ化して登録できる() {

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

        verify(passwordEncoder, never())
                .encode(any());
    }


    // =========================
    // findAll
    // =========================

    @Test
    void findAll_ID昇順で取得する() {

        AppUser user1 = new AppUser();
        user1.setId(1L);

        AppUser user2 = new AppUser();
        user2.setId(2L);

        List<AppUser> users =
                List.of(user1, user2);

        Sort expectedSort =
                Sort.by(
                        Sort.Direction.ASC,
                        "id");

        when(appUserRepository.findAll(expectedSort))
                .thenReturn(users);

        List<AppUser> result =
                appUserService.findAll();

        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(appUserRepository)
                .findAll(expectedSort);
    }


    // =========================
    // findById
    // =========================

    @Test
    void findById_存在するユーザーを取得できる() {

        AppUser appUser = new AppUser();
        appUser.setId(1L);
        appUser.setUsername("admin");

        when(appUserRepository.findById(1L))
                .thenReturn(Optional.of(appUser));

        AppUser result =
                appUserService.findById(1L);

        assertEquals(
                1L,
                result.getId());

        assertEquals(
                "admin",
                result.getUsername());
    }

    @Test
    void findById_存在しないユーザーIDなら例外になる() {

        when(appUserRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                AppUserNotFoundException.class,
                () -> appUserService.findById(999L));
    }


    // =========================
    // findByUsername
    // =========================

    @Test
    void findByUsername_存在するユーザーを取得できる() {

        AppUser appUser = new AppUser();
        appUser.setId(1L);
        appUser.setUsername("admin");

        when(appUserRepository.findByUsername("admin"))
                .thenReturn(Optional.of(appUser));

        AppUser result =
                appUserService.findByUsername("admin");

        assertEquals(
                "admin",
                result.getUsername());
    }

    @Test
    void findByUsername_存在しないユーザー名なら例外になる() {

        when(appUserRepository.findByUsername("unknown"))
                .thenReturn(Optional.empty());

        assertThrows(
                AppUserNotFoundException.class,
                () -> appUserService.findByUsername("unknown"));
    }


    // =========================
    // update
    // =========================

    @Test
    void update_他ユーザーの情報を変更できる() {

        AppUser appUser = new AppUser();
        appUser.setId(2L);
        appUser.setUsername("user");
        appUser.setRole(Role.USER);

        AppUserEditForm form = new AppUserEditForm();
        form.setId(2L);
        form.setUsername("newuser");
        form.setRole(Role.ADMIN);

        when(appUserRepository.findById(2L))
                .thenReturn(Optional.of(appUser));

        appUserService.update(
                form,
                "admin");

        assertEquals(
                "newuser",
                appUser.getUsername());

        assertEquals(
                Role.ADMIN,
                appUser.getRole());

        verify(appUserRepository)
                .save(appUser);
    }

    @Test
    void update_存在しないユーザーなら例外になる() {

        AppUserEditForm form = new AppUserEditForm();
        form.setId(999L);
        form.setUsername("user");
        form.setRole(Role.USER);

        when(appUserRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                AppUserNotFoundException.class,
                () -> appUserService.update(
                        form,
                        "admin"));

        verify(appUserRepository, never())
                .save(any(AppUser.class));
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
    void update_最後のADMINをUSERへ変更しようとすると例外になる() {

        AppUser appUser = new AppUser();
        appUser.setId(2L);
        appUser.setUsername("admin2");
        appUser.setRole(Role.ADMIN);

        AppUserEditForm form = new AppUserEditForm();
        form.setId(2L);
        form.setUsername("admin2");
        form.setRole(Role.USER);

        when(appUserRepository.findById(2L))
                .thenReturn(Optional.of(appUser));

        when(appUserRepository.countByRole(Role.ADMIN))
                .thenReturn(1L);

        assertThrows(
                UserOperationException.class,
                () -> appUserService.update(
                        form,
                        "admin1"));

        verify(appUserRepository, never())
                .save(appUser);
    }

    @Test
    void update_ADMINが2人以上なら他のADMINをUSERへ変更できる() {

        AppUser appUser = new AppUser();
        appUser.setId(2L);
        appUser.setUsername("admin2");
        appUser.setRole(Role.ADMIN);

        AppUserEditForm form = new AppUserEditForm();
        form.setId(2L);
        form.setUsername("admin2");
        form.setRole(Role.USER);

        when(appUserRepository.findById(2L))
                .thenReturn(Optional.of(appUser));

        when(appUserRepository.countByRole(Role.ADMIN))
                .thenReturn(2L);

        appUserService.update(
                form,
                "admin1");

        assertEquals(
                Role.USER,
                appUser.getRole());

        verify(appUserRepository)
                .save(appUser);
    }


    // =========================
    // delete
    // =========================

    @Test
    void delete_他ユーザーを削除できる() {

        AppUser appUser = new AppUser();
        appUser.setId(2L);
        appUser.setUsername("user");
        appUser.setRole(Role.USER);

        when(appUserRepository.findById(2L))
                .thenReturn(Optional.of(appUser));

        appUserService.delete(
                2L,
                "admin");

        verify(appUserRepository)
                .delete(appUser);
    }

    @Test
    void delete_存在しないユーザーなら例外になる() {

        when(appUserRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                AppUserNotFoundException.class,
                () -> appUserService.delete(
                        999L,
                        "admin"));

        verify(appUserRepository, never())
                .delete(any(AppUser.class));
    }

    @Test
    void delete_自分自身を削除しようとすると例外になる() {

        AppUser appUser = new AppUser();
        appUser.setId(1L);
        appUser.setUsername("admin");
        appUser.setRole(Role.ADMIN);

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
    void delete_最後のADMINを削除しようとすると例外になる() {

        AppUser appUser = new AppUser();
        appUser.setId(2L);
        appUser.setUsername("admin2");
        appUser.setRole(Role.ADMIN);

        when(appUserRepository.findById(2L))
                .thenReturn(Optional.of(appUser));

        when(appUserRepository.countByRole(Role.ADMIN))
                .thenReturn(1L);

        assertThrows(
                UserOperationException.class,
                () -> appUserService.delete(
                        2L,
                        "admin1"));

        verify(appUserRepository, never())
                .delete(appUser);
    }

    @Test
    void delete_ADMINが2人以上なら他のADMINを削除できる() {

        AppUser appUser = new AppUser();
        appUser.setId(2L);
        appUser.setUsername("admin2");
        appUser.setRole(Role.ADMIN);

        when(appUserRepository.findById(2L))
                .thenReturn(Optional.of(appUser));

        when(appUserRepository.countByRole(Role.ADMIN))
                .thenReturn(2L);

        appUserService.delete(
                2L,
                "admin1");

        verify(appUserRepository)
                .delete(appUser);
    }


    // =========================
    // updatePassword
    // =========================

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
    void updatePassword_存在しないユーザーなら例外になる() {

        AppUserPasswordForm form =
                new AppUserPasswordForm();

        form.setId(999L);
        form.setPassword("newPassword");
        form.setConfirmPassword("newPassword");

        when(appUserRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                AppUserNotFoundException.class,
                () -> appUserService.updatePassword(form));

        verify(passwordEncoder, never())
                .encode(any());

        verify(appUserRepository, never())
                .save(any(AppUser.class));
    }
}