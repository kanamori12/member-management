package com.example.demo.service;

import java.util.List;
import com.example.demo.model.Member;
import com.example.demo.model.MemberSearchCondition;

public interface MemberService {

    void register(Member member);

    List<Member> findAll();

    List<Member> search(MemberSearchCondition condition);

    void delete(Long id);

    Member findById(Long id);

    void update(Member member);
}