package com.example.demo.service;

import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.exception.MemberNotFoundException;
import com.example.demo.model.Member;
import com.example.demo.model.MemberSearchCondition;
import com.example.demo.repository.MemberRepository;

@Service
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;

    public MemberServiceImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public void register(Member member) {
        memberRepository.save(member);
    }

    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Override
    public List<Member> search(MemberSearchCondition condition) {

        Specification<Member> specification = createSpecification(condition);

        Sort sort = createSort(condition.getSort());

        return memberRepository.findAll(specification, sort);
    }

    @Override
    public void delete(Long id) {

        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));

        memberRepository.delete(member);
    }

    @Override
    public Member findById(Long id) {

        return memberRepository.findById(id)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    @Override
    @Transactional
    public void update(Member member) {

        Member existingMember = memberRepository.findById(member.getId())
                .orElseThrow(() -> new MemberNotFoundException(member.getId()));

        existingMember.setName(member.getName());
        existingMember.setAge(member.getAge());
        existingMember.setMemberType(member.getMemberType());
    }

    private Specification<Member> createSpecification(
            MemberSearchCondition condition) {

        Specification<Member> specification = Specification.unrestricted();

        // 名前
        if (condition.getName() != null
                && !condition.getName().isBlank()) {

            String name = condition.getName().trim();

            specification = specification.and(
                    (root, query, cb) -> cb.like(
                            root.get("name"),
                            "%" + name + "%"));
        }

        // 年齢
        if (condition.getAge() != null) {

            Integer age = condition.getAge();

            specification = specification.and(
                    (root, query, cb) -> cb.equal(root.get("age"), age));
        }

        // 会員種別
        if (condition.getMemberType() != null
                && !condition.getMemberType().isBlank()) {

            String memberType = condition.getMemberType();

            specification = specification.and(
                    (root, query, cb) -> cb.equal(
                            root.get("memberType"),
                            memberType));
        }

        return specification;
    }

    private Sort createSort(String sort) {

        if ("idDesc".equals(sort)) {
            return Sort.by(Sort.Direction.DESC, "id");
        }

        if ("nameAsc".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "name");
        }

        if ("ageAsc".equals(sort)) {
            return Sort.by(Sort.Direction.ASC, "age");
        }

        // idAsc または値が不明な場合
        return Sort.by(Sort.Direction.ASC, "id");
    }
}