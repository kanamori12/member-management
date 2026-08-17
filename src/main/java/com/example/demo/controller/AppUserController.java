package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.model.AppUser;
import com.example.demo.model.AppUserEditForm;
import com.example.demo.model.AppUserForm;
import com.example.demo.model.AppUserPasswordForm;
import com.example.demo.service.AppUserService;

import jakarta.validation.Valid;

@Controller
public class AppUserController {

    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }

    @GetMapping("/users")
    public String userList(Model model) {

        model.addAttribute(
                "users",
                appUserService.findAll());

        return "user-list";
    }

    @GetMapping("/users/register")
    public String userRegister(Model model) {

        model.addAttribute(
                "appUserForm",
                new AppUserForm());

        return "user-register";
    }

    @PostMapping("/users/register")
    public String userRegister(
            @Valid AppUserForm appUserForm,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "user-register";
        }

        appUserService.register(appUserForm);

        return "redirect:/users?registered";
    }

    @GetMapping("/users/edit")
    public String userEdit(
            Long id,
            Model model) {

        AppUser appUser =
                appUserService.findById(id);

        AppUserEditForm form =
                new AppUserEditForm();

        form.setId(appUser.getId());
        form.setUsername(appUser.getUsername());
        form.setRole(appUser.getRole());

        model.addAttribute(
                "appUserEditForm",
                form);

        return "user-edit";
    }

    @PostMapping("/users/update")
    public String userUpdate(
            @Valid AppUserEditForm appUserEditForm,
            BindingResult bindingResult,
            Authentication authentication) {

        if (bindingResult.hasErrors()) {
            return "user-edit";
        }

        appUserService.update(
                appUserEditForm,
                authentication.getName());

        return "redirect:/users?updated";
    }

    @GetMapping("/users/delete")
    public String userDelete(
            Long id,
            Authentication authentication) {

        appUserService.delete(
                id,
                authentication.getName());

        return "redirect:/users?deleted";
    }

    @GetMapping("/users/password")
    public String userPassword(
            Long id,
            Model model) {

        AppUser appUser =
                appUserService.findById(id);

        AppUserPasswordForm form =
                new AppUserPasswordForm();

        form.setId(appUser.getId());

        model.addAttribute(
                "appUserPasswordForm",
                form);

        model.addAttribute(
                "username",
                appUser.getUsername());

        return "user-password";
    }

    @PostMapping("/users/password")
    public String userPasswordUpdate(
            @Valid AppUserPasswordForm appUserPasswordForm,
            BindingResult bindingResult,
            Model model) {

        if (!java.util.Objects.equals(
                appUserPasswordForm.getPassword(),
                appUserPasswordForm.getConfirmPassword())) {

            bindingResult.rejectValue(
                    "confirmPassword",
                    "passwordMismatch",
                    "パスワードが一致しません。");
        }

        if (bindingResult.hasErrors()) {

            AppUser appUser =
                    appUserService.findById(
                            appUserPasswordForm.getId());

            model.addAttribute(
                    "username",
                    appUser.getUsername());

            return "user-password";
        }

        appUserService.updatePassword(
                appUserPasswordForm);

        return "redirect:/users?passwordChanged";
    }
}