package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.exception.AppUserNotFoundException;
import com.example.demo.exception.UserOperationException;
import com.example.demo.model.AppUser;
import com.example.demo.model.AppUserEditForm;
import com.example.demo.model.AppUserForm;
import com.example.demo.model.AppUserPasswordForm;
import com.example.demo.model.Role;
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

    // =========================
    // 初期ユーザー登録
    // =========================

    public void register(
            String username,
            String rawPassword,
            Role role) {

        AppUser appUser = new AppUser();

        appUser.setUsername(username);

        appUser.setPassword(
                passwordEncoder.encode(rawPassword));

        appUser.setRole(role);

        appUserRepository.save(appUser);
    }


    // =========================
    // ユーザー登録
    // =========================

    public void register(AppUserForm form) {

        if (appUserRepository
                .findByUsername(form.getUsername())
                .isPresent()) {

            throw new UserOperationException(
                    "そのユーザー名はすでに使用されています。");
        }

        AppUser appUser = new AppUser();

        appUser.setUsername(
                form.getUsername());

        appUser.setPassword(
                passwordEncoder.encode(
                        form.getPassword()));

        appUser.setRole(
                form.getRole());

        appUserRepository.save(appUser);
    }


    // =========================
    // ユーザー一覧
    // =========================

    public List<AppUser> findAll() {

        return appUserRepository.findAll(
                Sort.by(
                        Sort.Direction.ASC,
                        "id"));
    }


    // =========================
    // IDからユーザー取得
    // =========================

    public AppUser findById(Long id) {

        return appUserRepository
                .findById(id)
                .orElseThrow(() ->
                        new AppUserNotFoundException(id));
    }


    // =========================
    // ユーザー名から取得
    // =========================

    public AppUser findByUsername(
            String username) {

        return appUserRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new AppUserNotFoundException(username));
    }


    // =========================
    // ユーザー情報更新
    // =========================

    public void update(
            AppUserEditForm form,
            String loginUsername) {

        AppUser appUser =
                findById(form.getId());

        boolean adminToUser =
                appUser.getRole() == Role.ADMIN
                        && form.getRole() == Role.USER;

        // 自分自身のADMIN権限変更は禁止
        if (appUser.getUsername().equals(loginUsername)
                && adminToUser) {

            throw new UserOperationException(
                    "自分自身のADMIN権限を変更することはできません。");
        }

        // 最後のADMINをUSERへ変更するのは禁止
        if (adminToUser
                && appUserRepository.countByRole(Role.ADMIN) <= 1) {

            throw new UserOperationException(
                    "最後のADMINユーザーの権限を変更することはできません。");
        }

        appUser.setUsername(
                form.getUsername());

        appUser.setRole(
                form.getRole());

        appUserRepository.save(appUser);
    }


    // =========================
    // ユーザー削除
    // =========================

    public void delete(
            Long id,
            String loginUsername) {

        AppUser appUser =
                findById(id);

        // 自分自身の削除は禁止
        if (appUser.getUsername().equals(loginUsername)) {

            throw new UserOperationException(
                    "自分自身を削除することはできません。");
        }

        // 最後のADMINの削除は禁止
        if (appUser.getRole() == Role.ADMIN
                && appUserRepository.countByRole(Role.ADMIN) <= 1) {

            throw new UserOperationException(
                    "最後のADMINユーザーを削除することはできません。");
        }

        appUserRepository.delete(appUser);
    }


    // =========================
    // パスワード変更
    // =========================

    public void updatePassword(
            AppUserPasswordForm form) {

        AppUser appUser =
                findById(form.getId());

        appUser.setPassword(
                passwordEncoder.encode(
                        form.getPassword()));

        appUserRepository.save(appUser);
    }
}