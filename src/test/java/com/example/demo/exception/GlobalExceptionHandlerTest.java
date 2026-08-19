package com.example.demo.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.ui.Model;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private Model model;

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleMemberNotFoundException_エラー画面を表示できる() {

        MemberNotFoundException exception =
                new MemberNotFoundException(999L);

        String result =
                handler.handleMemberNotFoundException(
                        exception,
                        model);

        assertEquals(
                "error",
                result);

        verify(model)
                .addAttribute(
                        "errorMessage",
                        exception.getMessage());
    }

    @Test
    void handleIllegalArgumentException_エラー画面を表示できる() {

        IllegalArgumentException exception =
                new IllegalArgumentException(
                        "不正な操作です。");

        String result =
                handler.handleIllegalArgumentException(
                        exception,
                        model);

        assertEquals(
                "error",
                result);

        verify(model)
                .addAttribute(
                        "errorMessage",
                        "不正な操作です。");
    }

    @Test
    void handleAppUserNotFoundException_エラー画面を表示できる() {

        AppUserNotFoundException exception =
                new AppUserNotFoundException(999L);

        String result =
                handler.handleAppUserNotFoundException(
                        exception,
                        model);

        assertEquals(
                "error",
                result);

        verify(model)
                .addAttribute(
                        "errorMessage",
                        exception.getMessage());
    }

    @Test
    void handleUserOperationException_エラー画面を表示できる() {

        UserOperationException exception =
                new UserOperationException(
                        "この操作は実行できません。");

        String result =
                handler.handleUserOperationException(
                        exception,
                        model);

        assertEquals(
                "error",
                result);

        verify(model)
                .addAttribute(
                        "errorMessage",
                        "この操作は実行できません。");
    }
}