package com.example.demo.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.model.AppUser;
import com.example.demo.model.Member;
import com.example.demo.model.MemberSearchCondition;
import com.example.demo.service.AppUserService;
import com.example.demo.service.MemberService;

import jakarta.validation.Valid;

@Controller
public class MemberController {

        private final MemberService memberService;
        private final AppUserService appUserService;

        public MemberController(
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
        public String register(
                        @Valid Member member,
                        BindingResult bindingResult,
                        Authentication authentication) {

                if (bindingResult.hasErrors()) {
                        return "index";
                }

                AppUser loginUser = appUserService.findByUsername(authentication.getName());

                member.setOwner(loginUser);

                memberService.register(member);

                return "redirect:/list";
        }

        @GetMapping("/list")
        public String list(
                        @RequestParam(defaultValue = "0") int page,
                        Authentication authentication,
                        Model model) {

                Pageable pageable = PageRequest.of(
                                page,
                                5,
                                Sort.by(Sort.Direction.ASC, "id"));

                Page<Member> memberPage = memberService.findAll(
                                authentication.getName(),
                                isAdmin(authentication),
                                pageable);

                model.addAttribute(
                                "members",
                                memberPage.getContent());

                model.addAttribute(
                                "memberPage",
                                memberPage);

                model.addAttribute(
                                "condition",
                                new MemberSearchCondition());

                model.addAttribute("searched", false);

                return "list";
        }

        @GetMapping("/search")
        public String search(
                        MemberSearchCondition condition,
                        @RequestParam(defaultValue = "0") int page,
                        Authentication authentication,
                        Model model) {

                Pageable pageable = PageRequest.of(page, 5);

                Page<Member> memberPage = memberService.search(
                                condition,
                                authentication.getName(),
                                isAdmin(authentication),
                                pageable);

                model.addAttribute(
                                "members",
                                memberPage.getContent());

                model.addAttribute(
                                "memberPage",
                                memberPage);

                model.addAttribute(
                                "condition",
                                condition);

                model.addAttribute(
                                "searched",
                                true);

                return "list";
        }

        @GetMapping("/detail")
        public String detail(
                        Long id,
                        Authentication authentication,
                        Model model) {

                Member member = memberService.findById(
                                id,
                                authentication.getName(),
                                isAdmin(authentication));

                model.addAttribute("member", member);

                return "detail";
        }

        @GetMapping("/edit")
        public String edit(
                        Long id,
                        Authentication authentication,
                        Model model) {

                Member member = memberService.findById(
                                id,
                                authentication.getName(),
                                isAdmin(authentication));

                // 現在設定されているタグを
                // カンマ区切りの文字列に変換する
                String tagNames = member.getTags()
                                .stream()
                                .map(tag -> tag.getName())
                                .sorted()
                                .collect(
                                                java.util.stream.Collectors.joining(", "));

                member.setTagNames(tagNames);

                model.addAttribute(
                                "member",
                                member);

                return "edit";
        }

        @PostMapping("/update")
        public String update(
                        @Valid Member member,
                        BindingResult bindingResult,
                        Authentication authentication) {

                if (bindingResult.hasErrors()) {
                        return "edit";
                }

                memberService.update(
                                member,
                                authentication.getName(),
                                isAdmin(authentication));

                return "redirect:/list";
        }

        @GetMapping("/delete")
        public String delete(
                        Long id,
                        Authentication authentication) {

                memberService.delete(
                                id,
                                authentication.getName(),
                                isAdmin(authentication));

                return "redirect:/list";
        }

        private boolean isAdmin(Authentication authentication) {

                return authentication.getAuthorities()
                                .stream()
                                .anyMatch(authority -> "ROLE_ADMIN".equals(
                                                authority.getAuthority()));
        }
}