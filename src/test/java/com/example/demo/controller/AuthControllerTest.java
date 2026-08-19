package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthControllerTest {

    private AuthController authController;

    @BeforeEach
    void setUp() {
        authController = new AuthController();
    }

    @Test
    void login_ログイン画面を表示できる() {

        String result =
                authController.login();

        assertEquals(
                "login",
                result);
    }

    @Test
    void accessDenied_403画面を表示できる() {

        String result =
                authController.accessDenied();

        assertEquals(
                "403",
                result);
    }
}