package com.example.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Sort;
import java.util.List;
import com.example.demo.model.AppUser;
import com.example.demo.model.AppUserEditForm;
import com.example.demo.model.AppUserForm;
import com.example.demo.model.AppUserPasswordForm;
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
                passwordEncoder.encode(rawPassword));

        appUser.setRole(role);

        appUserRepository.save(appUser);
    }

    public void register(AppUserForm form) {

        if (appUserRepository.findByUsername(form.getUsername()).isPresent()) {
            throw new IllegalArgumentException(
                    "そのユーザー名はすでに使用されています。");
        }

        AppUser appUser = new AppUser();

        appUser.setUsername(form.getUsername());

        appUser.setPassword(
                passwordEncoder.encode(form.getPassword()));

        appUser.setRole(form.getRole());

        appUserRepository.save(appUser);
    }

    public List<AppUser> findAll() {
        return appUserRepository.findAll(
                Sort.by(Sort.Direction.ASC, "id"));
    }

    public AppUser findById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ログインユーザーが見つかりません。ID: " + id));
    }

    public void update(AppUserEditForm form, String loginUsername) {

        AppUser appUser = appUserRepository.findById(form.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ログインユーザーが見つかりません。ID: " + form.getId()));

        // 自分自身のADMIN権限をUSERへ変更するのは禁止
        if (appUser.getUsername().equals(loginUsername)
                && "ADMIN".equals(appUser.getRole())
                && "USER".equals(form.getRole())) {

            throw new IllegalArgumentException(
                    "自分自身のADMIN権限を変更することはできません。");
        }

        appUser.setUsername(form.getUsername());
        appUser.setRole(form.getRole());

        appUserRepository.save(appUser);
    }

    public void delete(Long id, String loginUsername) {

        AppUser appUser = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "ログインユーザーが見つかりません。ID: " + id));

        if (appUser.getUsername().equals(loginUsername)) {
            throw new IllegalArgumentException(
                    "自分自身を削除することはできません。");
        }

        appUserRepository.delete(appUser);
    }

    public void updatePassword(AppUserPasswordForm form) {

        AppUser appUser = appUserRepository.findById(form.getId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "ログインユーザーが見つかりません。ID: " + form.getId()));

        appUser.setPassword(
                passwordEncoder.encode(form.getPassword()));

        appUserRepository.save(appUser);
    }
}