package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.example.demo.model.AppUser;
import com.example.demo.model.AppUserEditForm;
import com.example.demo.model.AppUserForm;
import com.example.demo.model.AppUserPasswordForm;
import com.example.demo.model.Role;
import com.example.demo.service.AppUserService;

@ExtendWith(MockitoExtension.class)
class AppUserControllerTest {

    @Mock
    private AppUserService appUserService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Authentication authentication;

    private AppUserController controller;

    @BeforeEach
    void setUp() {

        controller =
                new AppUserController(appUserService);
    }


    // =========================
    // ユーザー一覧
    // =========================

    @Test
    void userList_ユーザー一覧を表示できる() {

        AppUser admin =
                createUser(
                        1L,
                        "admin",
                        Role.ADMIN);

        AppUser user =
                createUser(
                        2L,
                        "user",
                        Role.USER);

        List<AppUser> users =
                List.of(admin, user);

        when(appUserService.findAll())
                .thenReturn(users);

        String result =
                controller.userList(model);

        assertEquals(
                "user-list",
                result);

        verify(appUserService)
                .findAll();

        verify(model)
                .addAttribute(
                        "users",
                        users);
    }


    // =========================
    // ユーザー登録画面
    // =========================

    @Test
    void userRegister_GET_登録画面を表示できる() {

        String result =
                controller.userRegister(model);

        assertEquals(
                "user-register",
                result);

        verify(model)
                .addAttribute(
                        org.mockito.ArgumentMatchers.eq("appUserForm"),
                        any(AppUserForm.class));
    }


    // =========================
    // ユーザー登録
    // =========================

    @Test
    void userRegister_POST_正常なら登録して一覧へリダイレクトする() {

        AppUserForm form =
                new AppUserForm();

        form.setUsername("testuser");
        form.setPassword("password");
        form.setRole(Role.USER);

        when(bindingResult.hasErrors())
                .thenReturn(false);

        String result =
                controller.userRegister(
                        form,
                        bindingResult);

        assertEquals(
                "redirect:/users?registered",
                result);

        verify(appUserService)
                .register(form);
    }

    @Test
    void userRegister_POST_入力エラーなら登録画面を再表示する() {

        AppUserForm form =
                new AppUserForm();

        when(bindingResult.hasErrors())
                .thenReturn(true);

        String result =
                controller.userRegister(
                        form,
                        bindingResult);

        assertEquals(
                "user-register",
                result);

        verify(appUserService, never())
                .register(any(AppUserForm.class));
    }


    // =========================
    // ユーザー編集画面
    // =========================

    @Test
    void userEdit_編集画面を表示できる() {

        AppUser appUser =
                createUser(
                        1L,
                        "admin",
                        Role.ADMIN);

        when(appUserService.findById(1L))
                .thenReturn(appUser);

        String result =
                controller.userEdit(
                        1L,
                        model);

        assertEquals(
                "user-edit",
                result);

        verify(appUserService)
                .findById(1L);

        ArgumentCaptor<Object> captor =
                ArgumentCaptor.forClass(Object.class);

        verify(model)
                .addAttribute(
                        org.mockito.ArgumentMatchers.eq("appUserEditForm"),
                        captor.capture());

        AppUserEditForm form =
                (AppUserEditForm) captor.getValue();

        assertEquals(
                1L,
                form.getId());

        assertEquals(
                "admin",
                form.getUsername());

        assertEquals(
                Role.ADMIN,
                form.getRole());
    }


    // =========================
    // ユーザー更新
    // =========================

    @Test
    void userUpdate_正常なら更新して一覧へリダイレクトする() {

        AppUserEditForm form =
                new AppUserEditForm();

        form.setId(2L);
        form.setUsername("user");
        form.setRole(Role.ADMIN);

        when(bindingResult.hasErrors())
                .thenReturn(false);

        when(authentication.getName())
                .thenReturn("admin");

        String result =
                controller.userUpdate(
                        form,
                        bindingResult,
                        authentication);

        assertEquals(
                "redirect:/users?updated",
                result);

        verify(appUserService)
                .update(
                        form,
                        "admin");
    }

    @Test
    void userUpdate_入力エラーなら編集画面を再表示する() {

        AppUserEditForm form =
                new AppUserEditForm();

        when(bindingResult.hasErrors())
                .thenReturn(true);

        String result =
                controller.userUpdate(
                        form,
                        bindingResult,
                        authentication);

        assertEquals(
                "user-edit",
                result);

        verify(appUserService, never())
                .update(
                        any(AppUserEditForm.class),
                        any());
    }


    // =========================
    // ユーザー削除
    // =========================

    @Test
    void userDelete_ユーザーを削除して一覧へリダイレクトする() {

        when(authentication.getName())
                .thenReturn("admin");

        String result =
                controller.userDelete(
                        2L,
                        authentication);

        assertEquals(
                "redirect:/users?deleted",
                result);

        verify(appUserService)
                .delete(
                        2L,
                        "admin");
    }


    // =========================
    // パスワード変更画面
    // =========================

    @Test
    void userPassword_GET_パスワード変更画面を表示できる() {

        AppUser appUser =
                createUser(
                        2L,
                        "user",
                        Role.USER);

        when(appUserService.findById(2L))
                .thenReturn(appUser);

        String result =
                controller.userPassword(
                        2L,
                        model);

        assertEquals(
                "user-password",
                result);

        verify(appUserService)
                .findById(2L);

        verify(model)
                .addAttribute(
                        "username",
                        "user");

        ArgumentCaptor<Object> captor =
                ArgumentCaptor.forClass(Object.class);

        verify(model)
                .addAttribute(
                        org.mockito.ArgumentMatchers.eq(
                                "appUserPasswordForm"),
                        captor.capture());

        AppUserPasswordForm form =
                (AppUserPasswordForm) captor.getValue();

        assertEquals(
                2L,
                form.getId());
    }


    // =========================
    // パスワード変更
    // =========================

    @Test
    void userPasswordUpdate_正常ならパスワードを変更して一覧へリダイレクトする() {

        AppUserPasswordForm form =
                new AppUserPasswordForm();

        form.setId(2L);
        form.setPassword("newPassword");
        form.setConfirmPassword("newPassword");

        when(bindingResult.hasErrors())
                .thenReturn(false);

        String result =
                controller.userPasswordUpdate(
                        form,
                        bindingResult,
                        model);

        assertEquals(
                "redirect:/users?passwordChanged",
                result);

        verify(appUserService)
                .updatePassword(form);
    }

    @Test
    void userPasswordUpdate_確認用パスワードが一致しなければエラーになる() {

        AppUserPasswordForm form =
                new AppUserPasswordForm();

        form.setId(2L);
        form.setPassword("password1");
        form.setConfirmPassword("password2");

        AppUser appUser =
                createUser(
                        2L,
                        "user",
                        Role.USER);

        when(bindingResult.hasErrors())
                .thenReturn(true);

        when(appUserService.findById(2L))
                .thenReturn(appUser);

        String result =
                controller.userPasswordUpdate(
                        form,
                        bindingResult,
                        model);

        assertEquals(
                "user-password",
                result);

        verify(bindingResult)
                .rejectValue(
                        "confirmPassword",
                        "passwordMismatch",
                        "パスワードが一致しません。");

        verify(appUserService)
                .findById(2L);

        verify(model)
                .addAttribute(
                        "username",
                        "user");

        verify(appUserService, never())
                .updatePassword(any(AppUserPasswordForm.class));
    }

    @Test
    void userPasswordUpdate_入力エラーならパスワード変更画面を再表示する() {

        AppUserPasswordForm form =
                new AppUserPasswordForm();

        form.setId(2L);
        form.setPassword("newPassword");
        form.setConfirmPassword("newPassword");

        AppUser appUser =
                createUser(
                        2L,
                        "user",
                        Role.USER);

        when(bindingResult.hasErrors())
                .thenReturn(true);

        when(appUserService.findById(2L))
                .thenReturn(appUser);

        String result =
                controller.userPasswordUpdate(
                        form,
                        bindingResult,
                        model);

        assertEquals(
                "user-password",
                result);

        verify(appUserService)
                .findById(2L);

        verify(model)
                .addAttribute(
                        "username",
                        "user");

        verify(bindingResult, never())
                .rejectValue(
                        any(),
                        any(),
                        any());

        verify(appUserService, never())
                .updatePassword(any(AppUserPasswordForm.class));
    }


    // =========================
    // テストデータ作成
    // =========================

    private AppUser createUser(
            Long id,
            String username,
            Role role) {

        AppUser appUser =
                new AppUser();

        appUser.setId(id);
        appUser.setUsername(username);
        appUser.setPassword("hashedPassword");
        appUser.setRole(role);

        return appUser;
    }
}