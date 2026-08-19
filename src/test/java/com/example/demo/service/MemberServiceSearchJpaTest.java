package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.demo.model.AppUser;
import com.example.demo.model.Member;
import com.example.demo.model.MemberSearchCondition;
import com.example.demo.model.Role;
import com.example.demo.model.Tag;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.MemberRepository;
import com.example.demo.repository.TagRepository;

@DataJpaTest
@Import(MemberServiceImpl.class)
class MemberServiceSearchJpaTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private MemberServiceImpl memberService;

    /*
     * MemberServiceImplのコンストラクタでTagServiceが必要なため、
     * Spring Context上にMock Beanを登録する。
     *
     * このテストでは検索処理のみを実行するので、
     * TagService自体の処理は使用しない。
     */
    @MockitoBean
    private TagService tagService;

    private AppUser user1;
    private AppUser user2;

    private Tag importantTag;
    private Tag yokohamaTag;
    private Tag followTag;


    @BeforeEach
    void setUp() {

        /*
         * member_tagはMember側が所有しているため、
         * Memberを先に削除してからTagを削除する。
         */
        memberRepository.deleteAll();
        tagRepository.deleteAll();
        appUserRepository.deleteAll();


        // =========================
        // USER作成
        // =========================

        user1 = new AppUser();
        user1.setUsername("user1");
        user1.setPassword("password");
        user1.setRole(Role.USER);

        user1 = appUserRepository.save(user1);


        user2 = new AppUser();
        user2.setUsername("user2");
        user2.setPassword("password");
        user2.setRole(Role.USER);

        user2 = appUserRepository.save(user2);


        // =========================
        // Tag作成
        // =========================

        importantTag = createTag("重要");
        yokohamaTag = createTag("横浜");
        followTag = createTag("要フォロー");


        // =========================
        // Member作成
        // =========================

        /*
         * user1
         *
         * 田中太郎
         *   - 重要
         *   - 横浜
         */
        Member tanakaTaro =
                createMember(
                        "田中太郎",
                        30,
                        "一般",
                        user1);

        tanakaTaro.getTags()
                .add(importantTag);

        tanakaTaro.getTags()
                .add(yokohamaTag);

        memberRepository.save(tanakaTaro);


        /*
         * user1
         *
         * 田中花子
         *   - 重要
         *   - 要フォロー
         */
        Member tanakaHanako =
                createMember(
                        "田中花子",
                        20,
                        "ゴールド",
                        user1);

        tanakaHanako.getTags()
                .add(importantTag);

        tanakaHanako.getTags()
                .add(followTag);

        memberRepository.save(tanakaHanako);


        /*
         * user2
         *
         * 佐藤一郎
         *   - 重要
         */
        Member satoIchiro =
                createMember(
                        "佐藤一郎",
                        30,
                        "一般",
                        user2);

        satoIchiro.getTags()
                .add(importantTag);

        memberRepository.save(satoIchiro);


        /*
         * user2
         *
         * 山田次郎
         *   - 横浜
         */
        Member yamadaJiro =
                createMember(
                        "山田次郎",
                        40,
                        "ゴールド",
                        user2);

        yamadaJiro.getTags()
                .add(yokohamaTag);

        memberRepository.save(yamadaJiro);


        /*
         * INSERTをこの時点でDBへ反映しておく。
         *
         * これにより検索時のSQLが分かりやすくなる。
         */
        memberRepository.flush();
    }


    // =========================
    // 名前検索
    // =========================

    @Test
    void search_名前で部分一致検索できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setName("田中");
        condition.setSort("idAsc");

        Pageable pageable =
                PageRequest.of(0, 10);

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        pageable);

        assertEquals(
                2,
                result.getTotalElements());

        List<String> names =
                result.getContent()
                        .stream()
                        .map(Member::getName)
                        .toList();

        assertEquals(
                List.of(
                        "田中太郎",
                        "田中花子"),
                names);
    }


    // =========================
    // 名前trim
    // =========================

    @Test
    void search_名前の前後空白を除去して検索できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setName("  田中  ");
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        PageRequest.of(0, 10));

        assertEquals(
                2,
                result.getTotalElements());
    }


    // =========================
    // 年齢検索
    // =========================

    @Test
    void search_年齢で完全一致検索できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setAge(30);
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        PageRequest.of(0, 10));

        assertEquals(
                2,
                result.getTotalElements());

        for (Member member : result.getContent()) {

            assertEquals(
                    30,
                    member.getAge());
        }
    }


    // =========================
    // 会員種別検索
    // =========================

    @Test
    void search_会員種別で完全一致検索できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setMemberType("ゴールド");
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        PageRequest.of(0, 10));

        assertEquals(
                2,
                result.getTotalElements());

        for (Member member : result.getContent()) {

            assertEquals(
                    "ゴールド",
                    member.getMemberType());
        }
    }


    // =========================
    // タグ検索
    // =========================

    @Test
    void search_タグ名で完全一致検索できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setTagName("重要");
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        PageRequest.of(0, 10));

        /*
         * 重要タグ：
         *
         * 田中太郎
         * 田中花子
         * 佐藤一郎
         */
        assertEquals(
                3,
                result.getTotalElements());

        List<String> names =
                result.getContent()
                        .stream()
                        .map(Member::getName)
                        .toList();

        assertEquals(
                List.of(
                        "田中太郎",
                        "田中花子",
                        "佐藤一郎"),
                names);
    }


    @Test
    void search_タグ名の前後空白を除去して検索できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setTagName("  横浜  ");
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        PageRequest.of(0, 10));

        /*
         * 横浜タグ：
         *
         * 田中太郎
         * 山田次郎
         */
        assertEquals(
                2,
                result.getTotalElements());
    }


    @Test
    void search_タグ名が空白なら検索条件に含めない() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setTagName("   ");
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        PageRequest.of(0, 10));

        assertEquals(
                4,
                result.getTotalElements());
    }


    // =========================
    // USER + タグ
    // =========================

    @Test
    void search_USERなら自分の会員だけタグ検索できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setTagName("重要");
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "user1",
                        false,
                        PageRequest.of(0, 10));

        /*
         * 「重要」タグ自体は
         * user2の佐藤一郎にも付いている。
         *
         * しかしuser1で検索しているので、
         *
         * 田中太郎
         * 田中花子
         *
         * の2件だけ。
         */
        assertEquals(
                2,
                result.getTotalElements());

        for (Member member : result.getContent()) {

            assertEquals(
                    "user1",
                    member.getOwner()
                            .getUsername());
        }
    }


    @Test
    void search_USERは同じタグを持つ他人の会員を取得できない() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setTagName("重要");
        condition.setName("佐藤");
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "user1",
                        false,
                        PageRequest.of(0, 10));

        /*
         * 佐藤一郎は「重要」を持っているが、
         * ownerはuser2。
         *
         * user1では取得できない。
         */
        assertEquals(
                0,
                result.getTotalElements());
    }


    // =========================
    // 複数条件
    // =========================

    @Test
    void search_複数条件をAND検索できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setName("田中");
        condition.setAge(20);
        condition.setMemberType("ゴールド");
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        PageRequest.of(0, 10));

        assertEquals(
                1,
                result.getTotalElements());

        Member member =
                result.getContent()
                        .get(0);

        assertEquals(
                "田中花子",
                member.getName());

        assertEquals(
                20,
                member.getAge());

        assertEquals(
                "ゴールド",
                member.getMemberType());
    }


    @Test
    void search_名前年齢会員種別タグをAND検索できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setName("田中");
        condition.setAge(20);
        condition.setMemberType("ゴールド");
        condition.setTagName("要フォロー");
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        PageRequest.of(0, 10));

        assertEquals(
                1,
                result.getTotalElements());

        Member member =
                result.getContent()
                        .get(0);

        assertEquals(
                "田中花子",
                member.getName());

        assertEquals(
                20,
                member.getAge());

        assertEquals(
                "ゴールド",
                member.getMemberType());
    }


    // =========================
    // 条件なし
    // =========================

    @Test
    void search_検索条件なしならADMINは全件取得できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        PageRequest.of(0, 10));

        assertEquals(
                4,
                result.getTotalElements());
    }


    // =========================
    // USER所有者条件
    // =========================

    @Test
    void search_USERなら自分の会員だけ検索できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "user1",
                        false,
                        PageRequest.of(0, 10));

        assertEquals(
                2,
                result.getTotalElements());

        for (Member member : result.getContent()) {

            assertEquals(
                    "user1",
                    member.getOwner()
                            .getUsername());
        }
    }


    // =========================
    // USER + 名前
    // =========================

    @Test
    void search_USERなら所有者条件と名前条件をAND検索できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setName("田中");
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "user1",
                        false,
                        PageRequest.of(0, 10));

        assertEquals(
                2,
                result.getTotalElements());

        for (Member member : result.getContent()) {

            assertEquals(
                    "user1",
                    member.getOwner()
                            .getUsername());
        }
    }


    // =========================
    // USERが他人の会員を検索できない
    // =========================

    @Test
    void search_USERは他人の会員を検索できない() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setName("佐藤");
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "user1",
                        false,
                        PageRequest.of(0, 10));

        assertEquals(
                0,
                result.getTotalElements());
    }


    // =========================
    // blank条件
    // =========================

    @Test
    void search_名前が空白なら検索条件に含めない() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setName("   ");
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        PageRequest.of(0, 10));

        assertEquals(
                4,
                result.getTotalElements());
    }


    @Test
    void search_会員種別が空白なら検索条件に含めない() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setMemberType("   ");
        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        PageRequest.of(0, 10));

        assertEquals(
                4,
                result.getTotalElements());
    }


    // =========================
    // ページング
    // =========================

    @Test
    void search_ページングできる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setSort("idAsc");

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        PageRequest.of(0, 2));

        assertEquals(
                2,
                result.getContent()
                        .size());

        assertEquals(
                4,
                result.getTotalElements());

        assertEquals(
                2,
                result.getTotalPages());
    }


    // =========================
    // テストデータ作成
    // =========================

    private Member createMember(
            String name,
            Integer age,
            String memberType,
            AppUser owner) {

        Member member =
                new Member();

        member.setName(name);
        member.setAge(age);
        member.setMemberType(memberType);
        member.setOwner(owner);

        return member;
    }


    private Tag createTag(String name) {

        Tag tag =
                new Tag();

        tag.setName(name);

        return tagRepository.save(tag);
    }
}