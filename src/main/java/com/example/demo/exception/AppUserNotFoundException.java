package com.example.demo.exception;

public class AppUserNotFoundException extends RuntimeException {

    public AppUserNotFoundException(Long id) {
        super("ログインユーザーが見つかりません。ID: " + id);
    }

    public AppUserNotFoundException(String username) {
        super("ログインユーザーが見つかりません: " + username);
    }
}