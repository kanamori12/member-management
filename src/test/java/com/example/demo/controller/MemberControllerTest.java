package com.example.demo.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.AppUser;
import com.example.demo.model.Member;
import com.example.demo.model.MemberSearchCondition;
import com.example.demo.model.Role;
import com.example.demo.service.AppUserService;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.MemberService;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @Mock
    private AppUserService appUserService;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private Model model;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private Authentication authentication;

    @Mock
    private MultipartFile profileImage;

    private MemberController controller;

    @BeforeEach
    void setUp() {

        controller = new MemberController(
                memberService,
                appUserService,
                fileStorageService);
    }

    // =========================
    // index
    // =========================

    @Test
    void index_登録画面を表示できる() {

        String result =
                controller.index(model);

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
    void register_正常なら画像付き会員を登録できる() {

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

        when(fileStorageService.saveProfileImage(profileImage))
                .thenReturn("profile.jpg");

        String result =
                controller.register(
                        member,
                        bindingResult,
                        profileImage,
                        authentication);

        assertEquals(
                "redirect:/list?registered=true",
                result);

        assertEquals(
                loginUser,
                member.getOwner());

        assertEquals(
                "profile.jpg",
                member.getProfileImageName());

        verify(fileStorageService)
                .saveProfileImage(profileImage);

        verify(memberService)
                .register(member);
    }

    @Test
    void register_画像なしでも登録できる() {

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

        when(fileStorageService.saveProfileImage(profileImage))
                .thenReturn(null);

        String result =
                controller.register(
                        member,
                        bindingResult,
                        profileImage,
                        authentication);

        assertEquals(
                "redirect:/list?registered=true",
                result);

        assertNull(
                member.getProfileImageName());

        verify(memberService)
                .register(member);
    }

    @Test
    void register_入力エラーなら登録画面を再表示する() {

        Member member =
                new Member();

        when(bindingResult.hasErrors())
                .thenReturn(true);

        String result =
                controller.register(
                        member,
                        bindingResult,
                        profileImage,
                        authentication);

        assertEquals(
                "index",
                result);

        verify(appUserService, never())
                .findByUsername(any());

        verify(fileStorageService, never())
                .saveProfileImage(any());

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

        Page<Member> page =
                new PageImpl<>(
                        List.of(
                                member1,
                                member2));

        when(memberService.findAll(
                eq("admin"),
                eq(true),
                any(Pageable.class)))
                .thenReturn(page);

        String result =
                controller.list(
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

        Page<Member> page =
                new PageImpl<>(
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

        String result =
                controller.list(
                        1,
                        authentication,
                        model);

        assertEquals(
                "list",
                result);

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(
                        Pageable.class);

        verify(memberService)
                .findAll(
                        eq("user"),
                        eq(false),
                        captor.capture());

        Pageable pageable =
                captor.getValue();

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

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setName("田中");
        condition.setSort("idAsc");

        Page<Member> page =
                new PageImpl<>(
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

        String result =
                controller.search(
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

        MemberSearchCondition condition =
                new MemberSearchCondition();

        Page<Member> page =
                Page.empty();

        when(memberService.search(
                eq(condition),
                eq("user"),
                eq(false),
                any(Pageable.class)))
                .thenReturn(page);

        String result =
                controller.search(
                        condition,
                        2,
                        authentication,
                        model);

        assertEquals(
                "list",
                result);

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(
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

        String result =
                controller.detail(
                        1L,
                        2,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
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

        verify(model)
                .addAttribute(
                        "page",
                        2);

        verify(model)
                .addAttribute(
                        "searched",
                        false);
    }

    @Test
    void detail_検索結果からなら検索状態をModelへ設定する() {

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

        String result =
                controller.detail(
                        1L,
                        1,
                        true,
                        "田中",
                        30,
                        "一般",
                        "重要",
                        "nameAsc",
                        authentication,
                        model);

        assertEquals(
                "detail",
                result);

        verify(model)
                .addAttribute(
                        "page",
                        1);

        verify(model)
                .addAttribute(
                        "searched",
                        true);

        verify(model)
                .addAttribute(
                        "name",
                        "田中");

        verify(model)
                .addAttribute(
                        "age",
                        30);

        verify(model)
                .addAttribute(
                        "memberType",
                        "一般");

        verify(model)
                .addAttribute(
                        "tagName",
                        "重要");

        verify(model)
                .addAttribute(
                        "sort",
                        "nameAsc");
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

        String result =
                controller.edit(
                        1L,
                        0,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        authentication,
                        model);

        assertEquals(
                "edit",
                result);

        verify(model)
                .addAttribute(
                        "member",
                        member);

        verify(model)
                .addAttribute(
                        "page",
                        0);
    }

    @Test
    void edit_検索結果からなら検索状態を保持する() {

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

        String result =
                controller.edit(
                        1L,
                        2,
                        true,
                        "田中",
                        30,
                        "一般",
                        "東京",
                        "ageAsc",
                        authentication,
                        model);

        assertEquals(
                "edit",
                result);

        verify(model)
                .addAttribute(
                        "searched",
                        true);

        verify(model)
                .addAttribute(
                        "name",
                        "田中");

        verify(model)
                .addAttribute(
                        "tagName",
                        "東京");

        verify(model)
                .addAttribute(
                        "sort",
                        "ageAsc");
    }

    // =========================
    // update
    // =========================

    @Test
    void update_画像変更なしなら更新して元ページへ戻る() {

        mockAuthentication(
                "admin",
                "ROLE_ADMIN");

        Member member = createMember(
                1L,
                "田中太郎",
                31,
                "ゴールド");

        member.setProfileImageName(
                "old.jpg");

        when(bindingResult.hasErrors())
                .thenReturn(false);

        when(profileImage.isEmpty())
                .thenReturn(true);

        String result =
                controller.update(
                        member,
                        bindingResult,
                        profileImage,
                        2,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        authentication,
                        model);

        assertEquals(
                "redirect:/list?page=2&updated=true",
                result);

        verify(memberService)
                .update(
                        member,
                        "admin",
                        true);

        verify(fileStorageService, never())
                .saveProfileImage(any());

        verify(fileStorageService, never())
                .deleteProfileImage(any());
    }

    @Test
    void update_画像変更時は新画像を保存して旧画像を削除する() {

        mockAuthentication(
                "admin",
                "ROLE_ADMIN");

        Member member = createMember(
                1L,
                "田中太郎",
                31,
                "ゴールド");

        member.setProfileImageName(
                "old.jpg");

        when(bindingResult.hasErrors())
                .thenReturn(false);

        when(profileImage.isEmpty())
                .thenReturn(false);

        when(fileStorageService.saveProfileImage(profileImage))
                .thenReturn("new.jpg");

        String result =
                controller.update(
                        member,
                        bindingResult,
                        profileImage,
                        0,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        authentication,
                        model);

        assertEquals(
                "redirect:/list?page=0&updated=true",
                result);

        assertEquals(
                "new.jpg",
                member.getProfileImageName());

        verify(fileStorageService)
                .saveProfileImage(
                        profileImage);

        verify(memberService)
                .update(
                        member,
                        "admin",
                        true);

        verify(fileStorageService)
                .deleteProfileImage(
                        "old.jpg");
    }

    @Test
    void update_DB更新後に旧画像を削除する() {

        mockAuthentication(
                "admin",
                "ROLE_ADMIN");

        Member member = createMember(
                1L,
                "田中",
                30,
                "一般");

        member.setProfileImageName(
                "old.jpg");

        when(bindingResult.hasErrors())
                .thenReturn(false);

        when(profileImage.isEmpty())
                .thenReturn(false);

        when(fileStorageService.saveProfileImage(profileImage))
                .thenReturn("new.jpg");

        controller.update(
                member,
                bindingResult,
                profileImage,
                0,
                false,
                null,
                null,
                null,
                null,
                null,
                authentication,
                model);

        InOrder inOrder =
                inOrder(
                        fileStorageService,
                        memberService);

        inOrder.verify(fileStorageService)
                .saveProfileImage(
                        profileImage);

        inOrder.verify(memberService)
                .update(
                        member,
                        "admin",
                        true);

        inOrder.verify(fileStorageService)
                .deleteProfileImage(
                        "old.jpg");
    }

    @Test
    void update_検索結果からなら検索条件を保持して戻る() {

        mockAuthentication(
                "user",
                "ROLE_USER");

        Member member = createMember(
                1L,
                "山田太郎",
                31,
                "ゴールド");

        when(bindingResult.hasErrors())
                .thenReturn(false);

        when(profileImage.isEmpty())
                .thenReturn(true);

        String result =
                controller.update(
                        member,
                        bindingResult,
                        profileImage,
                        2,
                        true,
                        "山田",
                        30,
                        "ゴールド",
                        "重要",
                        "ageAsc",
                        authentication,
                        model);

        assertEquals(
                "redirect:/search"
                        + "?page=2"
                        + "&name=%E5%B1%B1%E7%94%B0"
                        + "&age=30"
                        + "&memberType=%E3%82%B4%E3%83%BC%E3%83%AB%E3%83%89"
                        + "&tagName=%E9%87%8D%E8%A6%81"
                        + "&sort=ageAsc"
                        + "&updated=true",
                result);

        verify(memberService)
                .update(
                        member,
                        "user",
                        false);
    }

    @Test
    void update_入力エラーなら編集画面を再表示する() {

        Member member =
                new Member();

        when(bindingResult.hasErrors())
                .thenReturn(true);

        String result =
                controller.update(
                        member,
                        bindingResult,
                        profileImage,
                        3,
                        true,
                        "田中",
                        30,
                        "一般",
                        "重要",
                        "idAsc",
                        authentication,
                        model);

        assertEquals(
                "edit",
                result);

        verify(model)
                .addAttribute(
                        "page",
                        3);

        verify(model)
                .addAttribute(
                        "searched",
                        true);

        verify(model)
                .addAttribute(
                        "name",
                        "田中");

        verify(model)
                .addAttribute(
                        "tagName",
                        "重要");

        verify(fileStorageService, never())
                .saveProfileImage(any());

        verify(memberService, never())
                .update(
                        any(Member.class),
                        any(),
                        any(Boolean.class));
    }

    // =========================
    // delete
    // =========================

    @Test
    void delete_ADMINなら削除して元ページへ戻る() {

        mockAuthentication(
                "admin",
                "ROLE_ADMIN");

        String result =
                controller.delete(
                        1L,
                        2,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        authentication);

        assertEquals(
                "redirect:/list?page=2&deleted=true",
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

        String result =
                controller.delete(
                        1L,
                        1,
                        false,
                        null,
                        null,
                        null,
                        null,
                        null,
                        authentication);

        assertEquals(
                "redirect:/list?page=1&deleted=true",
                result);

        verify(memberService)
                .delete(
                        1L,
                        "user",
                        false);
    }

    @Test
    void delete_検索結果からなら検索条件を保持して戻る() {

        mockAuthentication(
                "admin",
                "ROLE_ADMIN");

        String result =
                controller.delete(
                        1L,
                        2,
                        true,
                        "田中",
                        30,
                        "ゴールド",
                        "重要",
                        "idDesc",
                        authentication);

        assertEquals(
                "redirect:/search"
                        + "?page=2"
                        + "&name=%E7%94%B0%E4%B8%AD"
                        + "&age=30"
                        + "&memberType=%E3%82%B4%E3%83%BC%E3%83%AB%E3%83%89"
                        + "&tagName=%E9%87%8D%E8%A6%81"
                        + "&sort=idDesc"
                        + "&deleted=true",
                result);

        verify(memberService)
                .delete(
                        1L,
                        "admin",
                        true);
    }

    // =========================
    // 共通
    // =========================

    private void mockAuthentication(
            String username,
            String role) {

        when(authentication.getName())
                .thenReturn(username);

        GrantedAuthority authority =
                () -> role;

        doReturn(
                List.of(authority))
                .when(authentication)
                .getAuthorities();
    }

    private Member createMember(
            Long id,
            String name,
            Integer age,
            String memberType) {

        Member member =
                new Member();

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

        AppUser appUser =
                new AppUser();

        appUser.setId(id);
        appUser.setUsername(username);
        appUser.setPassword(
                "hashedPassword");
        appUser.setRole(role);

        return appUser;
    }
}