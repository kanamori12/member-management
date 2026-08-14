package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.Objects;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.security.core.Authentication;
import com.example.demo.model.AppUser;
import com.example.demo.model.AppUserEditForm;
import com.example.demo.model.AppUserForm;
import com.example.demo.model.AppUserPasswordForm;
import com.example.demo.model.Member;
import com.example.demo.model.MemberSearchCondition;
import com.example.demo.service.AppUserService;
import com.example.demo.service.MemberService;

@Controller
public class HomeController {

    private final MemberService memberService;
    private final AppUserService appUserService;

    public HomeController(
            MemberService memberService,
            AppUserService appUserService) {

        this.memberService = memberService;
        this.appUserService = appUserService;
    }

    @GetMapping("/")
    public String index(Model model) {

        model.addAttribute("member", new Member());

        return "index";
    }

    @PostMapping("/register")
    public String register(@Valid Member member,
            BindingResult bindingResult,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "index";
        }

        memberService.register(member);

        return "redirect:/list";
    }

    @GetMapping("/list")
    public String list(Model model) {

        model.addAttribute("members", memberService.findAll());
        model.addAttribute("condition", new MemberSearchCondition());

        model.addAttribute("searched", false);

        return "list";
    }

    @GetMapping("/search")
    public String search(
            MemberSearchCondition condition,
            Model model) {

        model.addAttribute(
                "members",
                memberService.search(condition));

        model.addAttribute("condition", condition);

        model.addAttribute("searched", true);

        return "list";
    }

    @GetMapping("/delete")
    public String delete(Long id) {

        memberService.delete(id);

        return "redirect:/list";
    }

    @GetMapping("/edit")
    public String edit(Long id, Model model) {

        Member member = memberService.findById(id);

        model.addAttribute("member", member);

        return "edit";
    }

    @PostMapping("/update")
    public String update(@Valid Member member,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "edit";
        }

        memberService.update(member);

        return "redirect:/list";
    }

    @GetMapping("/detail")
    public String detail(Long id, Model model) {

        Member member = memberService.findById(id);

        model.addAttribute("member", member);

        return "detail";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/403")
    public String accessDenied() {
        return "403";
    }

    @GetMapping("/users/register")
    public String userRegister(Model model) {

        model.addAttribute("appUserForm", new AppUserForm());

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

    @GetMapping("/users")
    public String userList(Model model) {

        model.addAttribute("users", appUserService.findAll());

        return "user-list";
    }

    @GetMapping("/users/edit")
    public String userEdit(Long id, Model model) {

        AppUser appUser = appUserService.findById(id);

        AppUserEditForm form = new AppUserEditForm();
        form.setId(appUser.getId());
        form.setUsername(appUser.getUsername());
        form.setRole(appUser.getRole());

        model.addAttribute("appUserEditForm", form);

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

        AppUser appUser = appUserService.findById(id);

        AppUserPasswordForm form = new AppUserPasswordForm();
        form.setId(appUser.getId());

        model.addAttribute("appUserPasswordForm", form);
        model.addAttribute("username", appUser.getUsername());

        return "user-password";
    }

    @PostMapping("/users/password")
    public String userPasswordUpdate(
            @Valid AppUserPasswordForm appUserPasswordForm,
            BindingResult bindingResult,
            Model model) {

        // パスワード一致チェック
        if (!Objects.equals(
                appUserPasswordForm.getPassword(),
                appUserPasswordForm.getConfirmPassword())) {

            bindingResult.rejectValue(
                    "confirmPassword",
                    "passwordMismatch",
                    "パスワードが一致しません。");
        }

        if (bindingResult.hasErrors()) {

            AppUser appUser = appUserService.findById(appUserPasswordForm.getId());

            model.addAttribute(
                    "username",
                    appUser.getUsername());

            return "user-password";
        }

        appUserService.updatePassword(appUserPasswordForm);

        return "redirect:/users?passwordChanged";
    }
}