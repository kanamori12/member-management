package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import com.example.demo.model.Member;
import com.example.demo.model.MemberSearchCondition;
import com.example.demo.service.MemberService;

@Controller
public class HomeController {

    private final MemberService memberService;

    public HomeController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/")
    public String index(Model model) {

        model.addAttribute("member", new Member());

        model.addAttribute("condition", new MemberSearchCondition());

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
            memberService.search(condition)
    );

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
}