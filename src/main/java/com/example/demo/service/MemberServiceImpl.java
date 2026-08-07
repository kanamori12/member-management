package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.Member;

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
    public List<Member> search(String keyword) {

        List<Member> result = new ArrayList<>();

        for (Member member : members) {

            if (member.getName().contains(keyword)) {
                result.add(member);
            }

        }

        return result;
    }

    @Override
    public void delete(Long id) {

        members.removeIf(member -> member.getId().equals(id));

    }
}