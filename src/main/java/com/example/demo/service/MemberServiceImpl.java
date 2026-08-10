package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

import org.springframework.stereotype.Service;

import com.example.demo.model.Member;
import com.example.demo.model.MemberSearchCondition;

@Service
public class MemberServiceImpl implements MemberService {

    private final List<Member> members = new ArrayList<>();

    private Long nextId = 1L;

    @Override
    public void register(Member member) {
        member.setId(nextId);
        nextId++;
        members.add(member);
    }

    @Override
    public List<Member> findAll() {
        return members;
    }

    @Override
    public List<Member> search(MemberSearchCondition condition) {

        List<Member> result = new ArrayList<>();

        for (Member member : members) {

            boolean match = true;

            // 名前
            if (condition.getName() != null
                    && !condition.getName().isBlank()) {

                if (!member.getName().contains(condition.getName().trim())) {
                    match = false;
                }
            }

            // 年齢
            if (condition.getAge() != null) {

                if (!member.getAge().equals(condition.getAge())) {
                    match = false;
                }
            }

            // 会員種別
            if (condition.getMemberType() != null
                    && !condition.getMemberType().isBlank()) {

                if (!member.getMemberType().equals(condition.getMemberType())) {
                    match = false;
                }
            }

            if (match) {
                result.add(member);
            }
        }
        if ("idAsc".equals(condition.getSort())) {

            result.sort(Comparator.comparing(Member::getId));

        } else if ("idDesc".equals(condition.getSort())) {

            result.sort(Comparator.comparing(Member::getId).reversed());

        } else if ("nameAsc".equals(condition.getSort())) {

            result.sort(Comparator.comparing(Member::getName));

        } else if ("ageAsc".equals(condition.getSort())) {

            result.sort(Comparator.comparing(Member::getAge));

        }
        return result;
    }

    @Override
    public void delete(Long id) {

        members.removeIf(member -> member.getId().equals(id));

    }

    @Override
    public Member findById(Long id) {

        for (Member member : members) {
            if (member.getId().equals(id)) {
                return member;
            }
        }

        return null;
    }

    @Override
    public void update(Member member) {

        Member oldMember = findById(member.getId());

        if (oldMember != null) {
            oldMember.setName(member.getName());
            oldMember.setAge(member.getAge());
            oldMember.setMemberType(member.getMemberType());
        }
    }
}