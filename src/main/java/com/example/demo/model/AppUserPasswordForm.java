package com.example.demo.model;

import jakarta.validation.constraints.NotBlank;

public class AppUserPasswordForm {

    private Long id;

    @NotBlank(message = "新しいパスワードを入力してください")
    private String password;

    @NotBlank(message = "確認用パスワードを入力してください")
    private String confirmPassword;

    public AppUserPasswordForm() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}