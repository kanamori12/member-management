package com.example.demo.model;

import jakarta.validation.constraints.NotBlank;

public class AppUserEditForm {

    private Long id;

    @NotBlank(message = "ユーザー名を入力してください")
    private String username;

    @NotBlank(message = "権限を選択してください")
    private String role;

    public AppUserEditForm() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}