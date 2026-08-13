package com.example.demo.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException(Long id) {
        super("会員が見つかりません。ID: " + id);
    }
}