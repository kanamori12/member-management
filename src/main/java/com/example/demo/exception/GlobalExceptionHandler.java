package com.example.demo.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    public String handleMemberNotFoundException(
            MemberNotFoundException e,
            Model model) {

        model.addAttribute("errorMessage", e.getMessage());

        return "error";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(
            IllegalArgumentException e,
            Model model) {

        model.addAttribute("errorMessage", e.getMessage());

        return "error";
    }

    @ExceptionHandler(AppUserNotFoundException.class)
    public String handleAppUserNotFoundException(
            AppUserNotFoundException e,
            Model model) {

        model.addAttribute("errorMessage", e.getMessage());

        return "error";
    }

    @ExceptionHandler(UserOperationException.class)
    public String handleUserOperationException(
            UserOperationException e,
            Model model) {

        model.addAttribute("errorMessage", e.getMessage());

        return "error";
    }
}