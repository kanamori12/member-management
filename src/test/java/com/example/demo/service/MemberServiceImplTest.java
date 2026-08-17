package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.exception.MemberNotFoundException;
import com.example.demo.model.Member;
import com.example.demo.repository.MemberRepository;

@ExtendWith(MockitoExtension.class)
public class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    private MemberServiceImpl memberService;

    @BeforeEach
    void setUp() {
        memberService = new MemberServiceImpl(memberRepository);
    }

    @Test
    void findById_管理者なら会員を取得できる() {

        Member member = new Member();
        member.setId(1L);
        member.setName("田中");
        member.setAge(30);
        member.setMemberType("一般");

        when(memberRepository.findById(1L))
                .thenReturn(Optional.of(member));

        Member result = memberService.findById(
                1L,
                "admin",
                true);

        assertEquals(1L, result.getId());
        assertEquals("田中", result.getName());
        assertEquals(30, result.getAge());
        assertEquals("一般", result.getMemberType());
    }

    @Test
    void findById_存在しないIDなら例外になる() {

        when(memberRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                MemberNotFoundException.class,
                () -> memberService.findById(
                        999L,
                        "admin",
                        true));
    }

    @Test
    void findById_USERなら自分の会員を取得できる() {

        Member member = new Member();
        member.setId(1L);
        member.setName("田中");
        member.setAge(30);
        member.setMemberType("一般");

        when(memberRepository.findByIdAndOwnerUsername(
                1L,
                "user"))
                .thenReturn(Optional.of(member));

        Member result = memberService.findById(
                1L,
                "user",
                false);

        assertEquals(1L, result.getId());
        assertEquals("田中", result.getName());
    }

    @Test
    void findById_USERが他人の会員を取得しようとすると例外になる() {

        when(memberRepository.findByIdAndOwnerUsername(
                2L,
                "user"))
                .thenReturn(Optional.empty());

        assertThrows(
                MemberNotFoundException.class,
                () -> memberService.findById(
                        2L,
                        "user",
                        false));
    }

    @Test
    void delete_USERなら自分の会員を削除できる() {

        Member member = new Member();
        member.setId(1L);

        when(memberRepository.findByIdAndOwnerUsername(
                1L,
                "user"))
                .thenReturn(Optional.of(member));

        memberService.delete(
                1L,
                "user",
                false);

        verify(memberRepository)
                .delete(member);
    }

    @Test
    void delete_USERが他人の会員を削除しようとすると例外になる() {

        when(memberRepository.findByIdAndOwnerUsername(
                2L,
                "user"))
                .thenReturn(Optional.empty());

        assertThrows(
                MemberNotFoundException.class,
                () -> memberService.delete(
                        2L,
                        "user",
                        false));
    }

    @Test
    void update_USERなら自分の会員を更新できる() {

        Member existingMember = new Member();
        existingMember.setId(1L);
        existingMember.setName("田中");
        existingMember.setAge(30);
        existingMember.setMemberType("一般");

        Member updateMember = new Member();
        updateMember.setId(1L);
        updateMember.setName("田中太郎");
        updateMember.setAge(31);
        updateMember.setMemberType("ゴールド");

        when(memberRepository.findByIdAndOwnerUsername(
                1L,
                "user"))
                .thenReturn(Optional.of(existingMember));

        memberService.update(
                updateMember,
                "user",
                false);

        assertEquals("田中太郎", existingMember.getName());
        assertEquals(31, existingMember.getAge());
        assertEquals("ゴールド", existingMember.getMemberType());
    }

    @Test
    void update_USERが他人の会員を更新しようとすると例外になる() {

        Member updateMember = new Member();
        updateMember.setId(2L);
        updateMember.setName("変更後");
        updateMember.setAge(40);
        updateMember.setMemberType("ゴールド");

        when(memberRepository.findByIdAndOwnerUsername(
                2L,
                "user"))
                .thenReturn(Optional.empty());

        assertThrows(
                MemberNotFoundException.class,
                () -> memberService.update(
                        updateMember,
                        "user",
                        false));
    }
}