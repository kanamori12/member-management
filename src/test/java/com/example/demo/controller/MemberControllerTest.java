package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import com.example.demo.model.AppUser;
import com.example.demo.model.Member;
import com.example.demo.model.MemberSearchCondition;
import com.example.demo.model.Role;
import com.example.demo.service.AppUserService;
import com.example.demo.service.MemberService;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @Mock
    private AppUserService appUserService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Authentication authentication;

    private MemberController controller;

    @BeforeEach
    void setUp() {
        controller = new MemberController(
                memberService,
                appUserService);
    }

    // =========================
    // index
    // =========================

    @Test
    void index_登録画面を表示できる() {

        String result = controller.index(model);

        assertEquals(
                "index",
                result);

        verify(model)
                .addAttribute(
                        eq("member"),
                        any(Member.class));
    }

    // =========================
    // register
    // =========================

    @Test
    void register_正常なら会員を登録して一覧へリダイレクトする() {

        Member member = createMember(
                1L,
                "田中",
                30,
                "一般");

        AppUser loginUser = createUser(
                1L,
                "user",
                Role.USER);

        when(bindingResult.hasErrors())
                .thenReturn(false);

        when(authentication.getName())
                .thenReturn("user");

        when(appUserService.findByUsername("user"))
                .thenReturn(loginUser);

        String result = controller.register(
                member,
                bindingResult,
                authentication);

        assertEquals(
                "redirect:/list",
                result);

        assertEquals(
                loginUser,
                member.getOwner());

        verify(appUserService)
                .findByUsername("user");

        verify(memberService)
                .register(member);
    }

    @Test
    void register_入力エラーなら登録画面を再表示する() {

        Member member = new Member();

        when(bindingResult.hasErrors())
                .thenReturn(true);

        String result = controller.register(
                member,
                bindingResult,
                authentication);

        assertEquals(
                "index",
                result);

        verify(appUserService, never())
                .findByUsername(any());

        verify(memberService, never())
                .register(any(Member.class));
    }

    // =========================
    // list
    // =========================

    @Test
    void list_ADMINなら全会員一覧を表示できる() {

        mockAuthentication(
                "admin",
                "ROLE_ADMIN");

        Member member1 = createMember(
                1L,
                "田中",
                30,
                "一般");

        Member member2 = createMember(
                2L,
                "佐藤",
                40,
                "ゴールド");

        Page<Member> page = new PageImpl<>(
                List.of(member1, member2));

        when(memberService.findAll(
                eq("admin"),
                eq(true),
                any(Pageable.class)))
                .thenReturn(page);

        String result = controller.list(
                0,
                authentication,
                model);

        assertEquals(
                "list",
                result);

        verify(memberService)
                .findAll(
                        eq("admin"),
                        eq(true),
                        any(Pageable.class));

        verify(model)
                .addAttribute(
                        "members",
                        page.getContent());

        verify(model)
                .addAttribute(
                        "memberPage",
                        page);

        verify(model)
                .addAttribute(
                        eq("condition"),
                        any(MemberSearchCondition.class));

        verify(model)
                .addAttribute(
                        "searched",
                        false);
    }

    @Test
    void list_USERなら自分の会員一覧を表示できる() {

        mockAuthentication(
                "user",
                "ROLE_USER");

        Page<Member> page = new PageImpl<>(
                List.of(
                        createMember(
                                1L,
                                "田中",
                                30,
                                "一般")));

        when(memberService.findAll(
                eq("user"),
                eq(false),
                any(Pageable.class)))
                .thenReturn(page);

        String result = controller.list(
                1,
                authentication,
                model);

        assertEquals(
                "list",
                result);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(
                Pageable.class);

        verify(memberService)
                .findAll(
                        eq("user"),
                        eq(false),
                        captor.capture());

        Pageable pageable = captor.getValue();

        assertEquals(
                1,
                pageable.getPageNumber());

        assertEquals(
                5,
                pageable.getPageSize());

        assertEquals(
                "id",
                pageable.getSort()
                        .iterator()
                        .next()
                        .getProperty());
    }

    // =========================
    // search
    // =========================

    @Test
    void search_ADMINなら検索結果を表示できる() {

        mockAuthentication(
                "admin",
                "ROLE_ADMIN");

        MemberSearchCondition condition = new MemberSearchCondition();

        condition.setName("田中");
        condition.setSort("idAsc");

        Page<Member> page = new PageImpl<>(
                List.of(
                        createMember(
                                1L,
                                "田中",
                                30,
                                "一般")));

        when(memberService.search(
                eq(condition),
                eq("admin"),
                eq(true),
                any(Pageable.class)))
                .thenReturn(page);

        String result = controller.search(
                condition,
                0,
                authentication,
                model);

        assertEquals(
                "list",
                result);

        verify(memberService)
                .search(
                        eq(condition),
                        eq("admin"),
                        eq(true),
                        any(Pageable.class));

        verify(model)
                .addAttribute(
                        "members",
                        page.getContent());

        verify(model)
                .addAttribute(
                        "memberPage",
                        page);

        verify(model)
                .addAttribute(
                        "condition",
                        condition);

        verify(model)
                .addAttribute(
                        "searched",
                        true);
    }

    @Test
    void search_USERならUSERとして検索する() {

        mockAuthentication(
                "user",
                "ROLE_USER");

        MemberSearchCondition condition = new MemberSearchCondition();

        Page<Member> page = Page.empty();

        when(memberService.search(
                eq(condition),
                eq("user"),
                eq(false),
                any(Pageable.class)))
                .thenReturn(page);

        String result = controller.search(
                condition,
                2,
                authentication,
                model);

        assertEquals(
                "list",
                result);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(
                Pageable.class);

        verify(memberService)
                .search(
                        eq(condition),
                        eq("user"),
                        eq(false),
                        captor.capture());

        assertEquals(
                2,
                captor.getValue()
                        .getPageNumber());

        assertEquals(
                5,
                captor.getValue()
                        .getPageSize());
    }

    // =========================
    // detail
    // =========================

    @Test
    void detail_ADMINなら会員詳細を表示できる() {

        mockAuthentication(
                "admin",
                "ROLE_ADMIN");

        Member member = createMember(
                1L,
                "田中",
                30,
                "一般");

        when(memberService.findById(
                1L,
                "admin",
                true))
                .thenReturn(member);

        String result = controller.detail(
                1L,
                authentication,
                model);

        assertEquals(
                "detail",
                result);

        verify(memberService)
                .findById(
                        1L,
                        "admin",
                        true);

        verify(model)
                .addAttribute(
                        "member",
                        member);
    }

    @Test
    void detail_USERならUSERとして会員詳細を取得する() {

        mockAuthentication(
                "user",
                "ROLE_USER");

        Member member = createMember(
                1L,
                "田中",
                30,
                "一般");

        when(memberService.findById(
                1L,
                "user",
                false))
                .thenReturn(member);

        String result = controller.detail(
                1L,
                authentication,
                model);

        assertEquals(
                "detail",
                result);

        verify(memberService)
                .findById(
                        1L,
                        "user",
                        false);
    }

    // =========================
    // edit
    // =========================

    @Test
    void edit_会員編集画面を表示できる() {

        mockAuthentication(
                "admin",
                "ROLE_ADMIN");

        Member member = createMember(
                1L,
                "田中",
                30,
                "一般");

        when(memberService.findById(
                1L,
                "admin",
                true))
                .thenReturn(member);

        String result = controller.edit(
                1L,
                authentication,
                model);

        assertEquals(
                "edit",
                result);

        verify(model)
                .addAttribute(
                        "member",
                        member);
    }

    // =========================
    // update
    // =========================

    @Test
    void update_正常なら更新して一覧へリダイレクトする() {

        mockAuthentication(
                "admin",
                "ROLE_ADMIN");

        Member member = createMember(
                1L,
                "田中太郎",
                31,
                "ゴールド");

        when(bindingResult.hasErrors())
                .thenReturn(false);

        String result = controller.update(
                member,
                bindingResult,
                authentication);

        assertEquals(
                "redirect:/list",
                result);

        verify(memberService)
                .update(
                        member,
                        "admin",
                        true);
    }

    @Test
    void update_USERならUSERとして更新する() {

        mockAuthentication(
                "user",
                "ROLE_USER");

        Member member = createMember(
                1L,
                "田中太郎",
                31,
                "ゴールド");

        when(bindingResult.hasErrors())
                .thenReturn(false);

        String result = controller.update(
                member,
                bindingResult,
                authentication);

        assertEquals(
                "redirect:/list",
                result);

        verify(memberService)
                .update(
                        member,
                        "user",
                        false);
    }

    @Test
    void update_入力エラーなら編集画面を再表示する() {

        Member member = new Member();

        when(bindingResult.hasErrors())
                .thenReturn(true);

        String result = controller.update(
                member,
                bindingResult,
                authentication);

        assertEquals(
                "edit",
                result);

        verify(memberService, never())
                .update(
                        any(Member.class),
                        any(),
                        eq(true));
    }

    // =========================
    // delete
    // =========================

    @Test
    void delete_ADMINなら削除して一覧へリダイレクトする() {

        mockAuthentication(
                "admin",
                "ROLE_ADMIN");

        String result = controller.delete(
                1L,
                authentication);

        assertEquals(
                "redirect:/list",
                result);

        verify(memberService)
                .delete(
                        1L,
                        "admin",
                        true);
    }

    @Test
    void delete_USERならUSERとして削除する() {

        mockAuthentication(
                "user",
                "ROLE_USER");

        String result = controller.delete(
                1L,
                authentication);

        assertEquals(
                "redirect:/list",
                result);

        verify(memberService)
                .delete(
                        1L,
                        "user",
                        false);
    }

    // =========================
    // 共通
    // =========================

    private void mockAuthentication(
            String username,
            String role) {

        when(authentication.getName())
                .thenReturn(username);

        GrantedAuthority authority = () -> role;

        doReturn(List.of(authority))
                .when(authentication)
                .getAuthorities();
    }

    private Member createMember(
            Long id,
            String name,
            Integer age,
            String memberType) {

        Member member = new Member();

        member.setId(id);
        member.setName(name);
        member.setAge(age);
        member.setMemberType(memberType);

        return member;
    }

    private AppUser createUser(
            Long id,
            String username,
            Role role) {

        AppUser appUser = new AppUser();

        appUser.setId(id);
        appUser.setUsername(username);
        appUser.setPassword("hashedPassword");
        appUser.setRole(role);

        return appUser;
    }
}