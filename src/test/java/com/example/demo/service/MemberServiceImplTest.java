package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import com.example.demo.exception.MemberNotFoundException;
import com.example.demo.model.Member;
import com.example.demo.model.MemberSearchCondition;
import com.example.demo.model.Tag;
import com.example.demo.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
public class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private TagService tagService;

    private MemberServiceImpl memberService;

    @BeforeEach
    void setUp() {

        memberService = new MemberServiceImpl(
                memberRepository,
                tagService);
    }


    // =========================
    // register
    // =========================

    @Test
    void register_タグなしで会員を登録できる() {

        Member member = createMember(
                1L,
                "田中",
                30,
                "一般");

        memberService.register(member);

        verify(memberRepository)
                .save(member);

        verify(tagService, never())
                .findOrCreate(any(String.class));
    }

    @Test
    void register_タグが空白でも会員を登録できる() {

        Member member = createMember(
                1L,
                "田中",
                30,
                "一般");

        member.setTagNames("   ");

        memberService.register(member);

        assertEquals(
                0,
                member.getTags().size());

        verify(tagService, never())
                .findOrCreate(any(String.class));

        verify(memberRepository)
                .save(member);
    }

    @Test
    void register_タグ付きで会員を登録できる() {

        Member member = createMember(
                1L,
                "田中",
                30,
                "一般");

        member.setTagNames(
                "重要, 横浜, 要フォロー");

        Tag important = createTag(
                1L,
                "重要");

        Tag yokohama = createTag(
                2L,
                "横浜");

        Tag follow = createTag(
                3L,
                "要フォロー");

        when(tagService.findOrCreate("重要"))
                .thenReturn(important);

        when(tagService.findOrCreate("横浜"))
                .thenReturn(yokohama);

        when(tagService.findOrCreate("要フォロー"))
                .thenReturn(follow);

        memberService.register(member);

        assertEquals(
                3,
                member.getTags().size());

        verify(tagService)
                .findOrCreate("重要");

        verify(tagService)
                .findOrCreate("横浜");

        verify(tagService)
                .findOrCreate("要フォロー");

        verify(memberRepository)
                .save(member);
    }

    @Test
    void register_タグの空白文字と重複を除去して登録できる() {

        Member member = createMember(
                1L,
                "田中",
                30,
                "一般");

        member.setTagNames(
                " 重要 , , 横浜 , 重要 ");

        Tag important = createTag(
                1L,
                "重要");

        Tag yokohama = createTag(
                2L,
                "横浜");

        when(tagService.findOrCreate("重要"))
                .thenReturn(important);

        when(tagService.findOrCreate("横浜"))
                .thenReturn(yokohama);

        memberService.register(member);

        assertEquals(
                2,
                member.getTags().size());

        verify(tagService)
                .findOrCreate("重要");

        verify(tagService)
                .findOrCreate("横浜");

        verify(memberRepository)
                .save(member);
    }


    // =========================
    // findAll
    // =========================

    @Test
    void findAll_ADMINなら全会員を取得できる() {

        Pageable pageable =
                PageRequest.of(0, 5);

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
                        List.of(member1, member2),
                        pageable,
                        2);

        when(memberRepository.findAll(pageable))
                .thenReturn(page);

        Page<Member> result =
                memberService.findAll(
                        "admin",
                        true,
                        pageable);

        assertEquals(
                2,
                result.getContent().size());

        assertEquals(
                "田中",
                result.getContent()
                        .get(0)
                        .getName());

        assertEquals(
                "佐藤",
                result.getContent()
                        .get(1)
                        .getName());

        verify(memberRepository)
                .findAll(pageable);

        verify(memberRepository, never())
                .findByOwnerUsername(
                        any(),
                        any(Pageable.class));
    }

    @Test
    void findAll_USERなら自分の会員だけ取得する() {

        Pageable pageable =
                PageRequest.of(0, 5);

        Member member = createMember(
                1L,
                "田中",
                30,
                "一般");

        Page<Member> page =
                new PageImpl<>(
                        List.of(member),
                        pageable,
                        1);

        when(memberRepository.findByOwnerUsername(
                "user",
                pageable))
                .thenReturn(page);

        Page<Member> result =
                memberService.findAll(
                        "user",
                        false,
                        pageable);

        assertEquals(
                1,
                result.getContent().size());

        assertEquals(
                "田中",
                result.getContent()
                        .get(0)
                        .getName());

        verify(memberRepository)
                .findByOwnerUsername(
                        "user",
                        pageable);

        verify(memberRepository, never())
                .findAll(pageable);
    }


    // =========================
    // search
    // =========================

    @Test
    void search_ID昇順で検索できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setSort("idAsc");

        Pageable pageable =
                PageRequest.of(0, 5);

        Member member = createMember(
                1L,
                "田中",
                30,
                "一般");

        Page<Member> page =
                new PageImpl<>(
                        List.of(member));

        when(memberRepository.findAll(
                any(Specification.class),
                any(Pageable.class)))
                .thenReturn(page);

        Page<Member> result =
                memberService.search(
                        condition,
                        "admin",
                        true,
                        pageable);

        assertEquals(
                1,
                result.getContent().size());

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(
                        Pageable.class);

        verify(memberRepository)
                .findAll(
                        any(Specification.class),
                        captor.capture());

        Pageable actualPageable =
                captor.getValue();

        assertEquals(
                0,
                actualPageable.getPageNumber());

        assertEquals(
                5,
                actualPageable.getPageSize());

        assertEquals(
                Sort.Direction.ASC,
                actualPageable
                        .getSort()
                        .getOrderFor("id")
                        .getDirection());
    }

    @Test
    void search_ID降順を指定できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setSort("idDesc");

        Pageable pageable =
                PageRequest.of(0, 5);

        when(memberRepository.findAll(
                any(Specification.class),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        memberService.search(
                condition,
                "admin",
                true,
                pageable);

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(
                        Pageable.class);

        verify(memberRepository)
                .findAll(
                        any(Specification.class),
                        captor.capture());

        Sort.Order order =
                captor.getValue()
                        .getSort()
                        .getOrderFor("id");

        assertEquals(
                Sort.Direction.DESC,
                order.getDirection());
    }

    @Test
    void search_名前昇順を指定できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setSort("nameAsc");

        Pageable pageable =
                PageRequest.of(0, 5);

        when(memberRepository.findAll(
                any(Specification.class),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        memberService.search(
                condition,
                "admin",
                true,
                pageable);

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(
                        Pageable.class);

        verify(memberRepository)
                .findAll(
                        any(Specification.class),
                        captor.capture());

        Sort.Order order =
                captor.getValue()
                        .getSort()
                        .getOrderFor("name");

        assertEquals(
                Sort.Direction.ASC,
                order.getDirection());
    }

    @Test
    void search_年齢昇順を指定できる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setSort("ageAsc");

        Pageable pageable =
                PageRequest.of(0, 5);

        when(memberRepository.findAll(
                any(Specification.class),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        memberService.search(
                condition,
                "admin",
                true,
                pageable);

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(
                        Pageable.class);

        verify(memberRepository)
                .findAll(
                        any(Specification.class),
                        captor.capture());

        Sort.Order order =
                captor.getValue()
                        .getSort()
                        .getOrderFor("age");

        assertEquals(
                Sort.Direction.ASC,
                order.getDirection());
    }

    @Test
    void search_不明な並び順ならID昇順になる() {

        MemberSearchCondition condition =
                new MemberSearchCondition();

        condition.setSort("unknown");

        Pageable pageable =
                PageRequest.of(0, 5);

        when(memberRepository.findAll(
                any(Specification.class),
                any(Pageable.class)))
                .thenReturn(Page.empty());

        memberService.search(
                condition,
                "admin",
                true,
                pageable);

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(
                        Pageable.class);

        verify(memberRepository)
                .findAll(
                        any(Specification.class),
                        captor.capture());

        Sort.Order order =
                captor.getValue()
                        .getSort()
                        .getOrderFor("id");

        assertEquals(
                Sort.Direction.ASC,
                order.getDirection());
    }


    // =========================
    // findById
    // =========================

    @Test
    void findById_ADMINなら会員を取得できる() {

        Member member = createMember(
                1L,
                "田中",
                30,
                "一般");

        when(memberRepository.findById(1L))
                .thenReturn(
                        Optional.of(member));

        Member result =
                memberService.findById(
                        1L,
                        "admin",
                        true);

        assertEquals(
                1L,
                result.getId());

        assertEquals(
                "田中",
                result.getName());

        assertEquals(
                30,
                result.getAge());

        assertEquals(
                "一般",
                result.getMemberType());

        verify(memberRepository)
                .findById(1L);
    }

    @Test
    void findById_ADMINで存在しない会員なら例外になる() {

        when(memberRepository.findById(999L))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                MemberNotFoundException.class,
                () -> memberService.findById(
                        999L,
                        "admin",
                        true));
    }

    @Test
    void findById_USERなら自分の会員を取得できる() {

        Member member = createMember(
                1L,
                "田中",
                30,
                "一般");

        when(memberRepository
                .findByIdAndOwnerUsername(
                        1L,
                        "user"))
                .thenReturn(
                        Optional.of(member));

        Member result =
                memberService.findById(
                        1L,
                        "user",
                        false);

        assertEquals(
                1L,
                result.getId());

        assertEquals(
                "田中",
                result.getName());

        verify(memberRepository)
                .findByIdAndOwnerUsername(
                        1L,
                        "user");

        verify(memberRepository, never())
                .findById(1L);
    }

    @Test
    void findById_USERが他人の会員を取得しようとすると例外になる() {

        when(memberRepository
                .findByIdAndOwnerUsername(
                        2L,
                        "user"))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                MemberNotFoundException.class,
                () -> memberService.findById(
                        2L,
                        "user",
                        false));
    }


    // =========================
    // update
    // =========================

    @Test
    void update_ADMINなら会員を更新できる() {

        Member existingMember =
                createMember(
                        1L,
                        "田中",
                        30,
                        "一般");

        Member updateMember =
                createMember(
                        1L,
                        "田中太郎",
                        31,
                        "ゴールド");

        when(memberRepository.findById(1L))
                .thenReturn(
                        Optional.of(existingMember));

        memberService.update(
                updateMember,
                "admin",
                true);

        assertEquals(
                "田中太郎",
                existingMember.getName());

        assertEquals(
                31,
                existingMember.getAge());

        assertEquals(
                "ゴールド",
                existingMember.getMemberType());

        assertEquals(
                0,
                existingMember.getTags().size());
    }

    @Test
    void update_USERなら自分の会員を更新できる() {

        Member existingMember =
                createMember(
                        1L,
                        "田中",
                        30,
                        "一般");

        Member updateMember =
                createMember(
                        1L,
                        "田中太郎",
                        31,
                        "ゴールド");

        when(memberRepository
                .findByIdAndOwnerUsername(
                        1L,
                        "user"))
                .thenReturn(
                        Optional.of(existingMember));

        memberService.update(
                updateMember,
                "user",
                false);

        assertEquals(
                "田中太郎",
                existingMember.getName());

        assertEquals(
                31,
                existingMember.getAge());

        assertEquals(
                "ゴールド",
                existingMember.getMemberType());
    }

    @Test
    void update_タグも更新できる() {

        Member existingMember =
                createMember(
                        1L,
                        "田中",
                        30,
                        "一般");

        Member updateMember =
                createMember(
                        1L,
                        "田中太郎",
                        31,
                        "ゴールド");

        updateMember.setTagNames(
                "重要, VIP");

        Tag important =
                createTag(
                        1L,
                        "重要");

        Tag vip =
                createTag(
                        2L,
                        "VIP");

        when(memberRepository.findById(1L))
                .thenReturn(
                        Optional.of(existingMember));

        when(tagService.findOrCreate("重要"))
                .thenReturn(important);

        when(tagService.findOrCreate("VIP"))
                .thenReturn(vip);

        memberService.update(
                updateMember,
                "admin",
                true);

        assertEquals(
                "田中太郎",
                existingMember.getName());

        assertEquals(
                31,
                existingMember.getAge());

        assertEquals(
                "ゴールド",
                existingMember.getMemberType());

        assertEquals(
                2,
                existingMember.getTags().size());

        assertEquals(
                "重要, VIP",
                existingMember.getTagNames());

        verify(tagService)
                .findOrCreate("重要");

        verify(tagService)
                .findOrCreate("VIP");
    }

    @Test
    void update_タグを空欄にするとタグをすべて解除できる() {

        Member existingMember =
                createMember(
                        1L,
                        "田中",
                        30,
                        "一般");

        Tag important =
                createTag(
                        1L,
                        "重要");

        existingMember.getTags()
                .add(important);

        Member updateMember =
                createMember(
                        1L,
                        "田中",
                        30,
                        "一般");

        updateMember.setTagNames("");

        when(memberRepository.findById(1L))
                .thenReturn(
                        Optional.of(existingMember));

        memberService.update(
                updateMember,
                "admin",
                true);

        assertEquals(
                0,
                existingMember.getTags().size());

        verify(tagService, never())
                .findOrCreate(any(String.class));
    }

    @Test
    void update_USERが他人の会員を更新しようとすると例外になる() {

        Member updateMember =
                createMember(
                        2L,
                        "変更後",
                        40,
                        "ゴールド");

        when(memberRepository
                .findByIdAndOwnerUsername(
                        2L,
                        "user"))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                MemberNotFoundException.class,
                () -> memberService.update(
                        updateMember,
                        "user",
                        false));
    }


    // =========================
    // delete
    // =========================

    @Test
    void delete_ADMINなら会員を削除できる() {

        Member member =
                createMember(
                        1L,
                        "田中",
                        30,
                        "一般");

        when(memberRepository.findById(1L))
                .thenReturn(
                        Optional.of(member));

        memberService.delete(
                1L,
                "admin",
                true);

        verify(memberRepository)
                .delete(member);
    }

    @Test
    void delete_ADMINで存在しない会員なら例外になる() {

        when(memberRepository.findById(999L))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                MemberNotFoundException.class,
                () -> memberService.delete(
                        999L,
                        "admin",
                        true));

        verify(memberRepository, never())
                .delete(any(Member.class));
    }

    @Test
    void delete_USERなら自分の会員を削除できる() {

        Member member =
                createMember(
                        1L,
                        "田中",
                        30,
                        "一般");

        when(memberRepository
                .findByIdAndOwnerUsername(
                        1L,
                        "user"))
                .thenReturn(
                        Optional.of(member));

        memberService.delete(
                1L,
                "user",
                false);

        verify(memberRepository)
                .delete(member);
    }

    @Test
    void delete_USERが他人の会員を削除しようとすると例外になる() {

        when(memberRepository
                .findByIdAndOwnerUsername(
                        2L,
                        "user"))
                .thenReturn(
                        Optional.empty());

        assertThrows(
                MemberNotFoundException.class,
                () -> memberService.delete(
                        2L,
                        "user",
                        false));

        verify(memberRepository, never())
                .delete(any(Member.class));
    }


    // =========================
    // テストデータ作成
    // =========================

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

    private Tag createTag(
            Long id,
            String name) {

        Tag tag =
                new Tag();

        tag.setId(id);
        tag.setName(name);

        return tag;
    }
}