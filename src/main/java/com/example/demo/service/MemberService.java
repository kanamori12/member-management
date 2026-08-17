package com.example.demo.service;

import com.example.demo.model.Member;
import com.example.demo.model.MemberSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {

    void register(Member member);

    Page<Member> findAll(
            String username,
            boolean admin,
            Pageable pageable);

    Page<Member> search(
            MemberSearchCondition condition,
            String username,
            boolean admin,
            Pageable pageable);

    Member findById(
            Long id,
            String username,
            boolean admin);

    void delete(
            Long id,
            String username,
            boolean admin);

    void update(
            Member member,
            String username,
            boolean admin);

}