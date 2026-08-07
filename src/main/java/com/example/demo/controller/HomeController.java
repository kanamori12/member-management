package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.model.Member;
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

        return "index";
    }

    @PostMapping("/register")
    public String register(Member member, Model model) {

        memberService.register(member);

        model.addAttribute("members", memberService.findAll());

        return "list";
    }

    @GetMapping("/list")
    public String list(Model model) {

        model.addAttribute("members", memberService.findAll());

        return "list";
    }

    @GetMapping("/search")
    public String search(String keyword, Model model) {

        model.addAttribute("members", memberService.search(keyword));

        return "list";
    }

    @GetMapping("/delete")
    public String delete(Long id) {

        memberService.delete(id);

        return "redirect:/list";
    }
}