package com.example.demo.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

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
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.model.AppUser;
import com.example.demo.model.Member;
import com.example.demo.model.MemberSearchCondition;
import com.example.demo.service.AppUserService;
import com.example.demo.service.FileStorageService;
import com.example.demo.service.MemberService;

import jakarta.validation.Valid;

@Controller
public class MemberController {

        private final MemberService memberService;
        private final AppUserService appUserService;
        private final FileStorageService fileStorageService;

        public MemberController(
                        MemberService memberService,
                        AppUserService appUserService,
                        FileStorageService fileStorageService) {

                this.memberService = memberService;
                this.appUserService = appUserService;
                this.fileStorageService = fileStorageService;
        }

        @GetMapping("/")
        public String index(Model model) {

                model.addAttribute(
                                "member",
                                new Member());

                return "index";
        }

        @PostMapping("/register")
        public String register(
                        @Valid Member member,
                        BindingResult bindingResult,
                        @RequestParam("profileImage") MultipartFile profileImage,
                        Authentication authentication) {

                if (bindingResult.hasErrors()) {
                        return "index";
                }

                AppUser loginUser = appUserService.findByUsername(
                                authentication.getName());

                member.setOwner(loginUser);

                String savedFilename = fileStorageService.saveProfileImage(
                                profileImage);

                member.setProfileImageName(
                                savedFilename);

                memberService.register(member);

                return "redirect:/list?registered=true";
        }

        @GetMapping("/list")
        public String list(
                        @RequestParam(defaultValue = "0") int page,
                        Authentication authentication,
                        Model model) {

                Pageable pageable = PageRequest.of(
                                page,
                                5,
                                Sort.by(
                                                Sort.Direction.ASC,
                                                "id"));

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

                model.addAttribute(
                                "searched",
                                false);

                return "list";
        }

        @GetMapping("/search")
        public String search(
                        MemberSearchCondition condition,
                        @RequestParam(defaultValue = "0") int page,
                        Authentication authentication,
                        Model model) {

                Pageable pageable = PageRequest.of(
                                page,
                                5);

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
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "false") boolean searched,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Integer age,
                        @RequestParam(required = false) String memberType,
                        @RequestParam(required = false) String tagName,
                        @RequestParam(required = false) String sort,
                        Authentication authentication,
                        Model model) {

                Member member = memberService.findById(
                                id,
                                authentication.getName(),
                                isAdmin(authentication));

                model.addAttribute(
                                "member",
                                member);

                addSearchState(
                                model,
                                page,
                                searched,
                                name,
                                age,
                                memberType,
                                tagName,
                                sort);

                return "detail";
        }

        @GetMapping("/edit")
        public String edit(
                        Long id,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "false") boolean searched,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Integer age,
                        @RequestParam(required = false) String memberType,
                        @RequestParam(required = false) String tagName,
                        @RequestParam(required = false) String sort,
                        Authentication authentication,
                        Model model) {

                Member member = memberService.findById(
                                id,
                                authentication.getName(),
                                isAdmin(authentication));

                String tagNames = member.getTags()
                                .stream()
                                .map(tag -> tag.getName())
                                .sorted()
                                .collect(
                                                Collectors.joining(", "));

                member.setTagNames(
                                tagNames);

                model.addAttribute(
                                "member",
                                member);

                addSearchState(
                                model,
                                page,
                                searched,
                                name,
                                age,
                                memberType,
                                tagName,
                                sort);

                return "edit";
        }

        @PostMapping("/update")
        public String update(
                        @Valid Member member,
                        BindingResult bindingResult,
                        @RequestParam("profileImage") MultipartFile profileImage,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "false") boolean searched,
                        @RequestParam(required = false) String searchName,
                        @RequestParam(required = false) Integer searchAge,
                        @RequestParam(required = false) String searchMemberType,
                        @RequestParam(required = false) String searchTagName,
                        @RequestParam(required = false) String searchSort,
                        Authentication authentication,
                        Model model) {

                if (bindingResult.hasErrors()) {

                        addSearchState(
                                        model,
                                        page,
                                        searched,
                                        searchName,
                                        searchAge,
                                        searchMemberType,
                                        searchTagName,
                                        searchSort);

                        return "edit";
                }

                /*
                 * 更新前の画像ファイル名を退避
                 */
                String oldFilename = member.getProfileImageName();

                /*
                 * 新しく保存した画像ファイル名
                 *
                 * null の場合は画像変更なし
                 */
                String newFilename = null;

                /*
                 * 新しい画像が選択されている場合
                 */
                if (profileImage != null
                                && !profileImage.isEmpty()) {

                        newFilename = fileStorageService.saveProfileImage(
                                        profileImage);

                        member.setProfileImageName(
                                        newFilename);
                }

                /*
                 * DB更新
                 *
                 * ここで例外が発生した場合、
                 * 旧画像はまだ削除されていない
                 */
                memberService.update(
                                member,
                                authentication.getName(),
                                isAdmin(authentication));

                /*
                 * DB更新成功後、
                 * 新しい画像へ変更した場合のみ
                 * 古い画像を削除
                 */
                if (newFilename != null
                                && oldFilename != null
                                && !oldFilename.isBlank()
                                && !oldFilename.equals(newFilename)) {

                        fileStorageService.deleteProfileImage(
                                        oldFilename);
                }

                /*
                 * 検索結果から編集した場合
                 */
                if (searched) {

                        return createSearchRedirect(
                                        page,
                                        searchName,
                                        searchAge,
                                        searchMemberType,
                                        searchTagName,
                                        searchSort)
                                        + "&updated=true";
                }

                /*
                 * 通常一覧から編集した場合
                 */
                return "redirect:/list?page="
                                + page
                                + "&updated=true";
        }

        @GetMapping("/delete")
        public String delete(
                        Long id,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "false") boolean searched,
                        @RequestParam(required = false) String name,
                        @RequestParam(required = false) Integer age,
                        @RequestParam(required = false) String memberType,
                        @RequestParam(required = false) String tagName,
                        @RequestParam(required = false) String sort,
                        Authentication authentication) {

                memberService.delete(
                                id,
                                authentication.getName(),
                                isAdmin(authentication));

                if (searched) {

                        return createSearchRedirect(
                                        page,
                                        name,
                                        age,
                                        memberType,
                                        tagName,
                                        sort)
                                        + "&deleted=true";
                }

                return "redirect:/list?page="
                                + page
                                + "&deleted=true";
        }

        private void addSearchState(
                        Model model,
                        int page,
                        boolean searched,
                        String name,
                        Integer age,
                        String memberType,
                        String tagName,
                        String sort) {

                model.addAttribute(
                                "page",
                                page);

                model.addAttribute(
                                "searched",
                                searched);

                model.addAttribute(
                                "name",
                                name);

                model.addAttribute(
                                "age",
                                age);

                model.addAttribute(
                                "memberType",
                                memberType);

                model.addAttribute(
                                "tagName",
                                tagName);

                model.addAttribute(
                                "sort",
                                sort);
        }

        private String createSearchRedirect(
                        int page,
                        String name,
                        Integer age,
                        String memberType,
                        String tagName,
                        String sort) {

                return "redirect:/search"
                                + "?page=" + page
                                + "&name=" + encode(name)
                                + "&age=" + value(age)
                                + "&memberType=" + encode(memberType)
                                + "&tagName=" + encode(tagName)
                                + "&sort=" + encode(sort);
        }

        private String encode(
                        String value) {

                if (value == null) {
                        return "";
                }

                return URLEncoder.encode(
                                value,
                                StandardCharsets.UTF_8);
        }

        private String value(
                        Integer value) {

                if (value == null) {
                        return "";
                }

                return value.toString();
        }

        private boolean isAdmin(
                        Authentication authentication) {

                return authentication
                                .getAuthorities()
                                .stream()
                                .anyMatch(
                                                authority -> "ROLE_ADMIN".equals(
                                                                authority.getAuthority()));
        }
}