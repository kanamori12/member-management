package com.example.demo.service;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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
    public Page<Member> findAll(
            String username,
            boolean admin,
            Pageable pageable) {

        if (admin) {
            return memberRepository.findAll(pageable);
        }

        return memberRepository.findByOwnerUsername(
                username,
                pageable);
    }

    @Override
    public Page<Member> search(
            MemberSearchCondition condition,
            String username,
            boolean admin,
            Pageable pageable) {

        Specification<Member> specification = createSpecification(
                condition,
                username,
                admin);

        Sort sort = createSort(condition.getSort());

        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                sort);

        return memberRepository.findAll(
                specification,
                sortedPageable);
    }

    @Override
    public void delete(
            Long id,
            String username,
            boolean admin) {

        Member member = findById(id, username, admin);

        memberRepository.delete(member);
    }

    @Override
    public Member findById(
            Long id,
            String username,
            boolean admin) {

        if (admin) {

            return memberRepository.findById(id)
                    .orElseThrow(() -> new MemberNotFoundException(id));
        }

        return memberRepository
                .findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    @Override
    @Transactional
    public void update(
            Member member,
            String username,
            boolean admin) {

        Member existingMember = findById(
                member.getId(),
                username,
                admin);

        existingMember.setName(member.getName());
        existingMember.setAge(member.getAge());
        existingMember.setMemberType(member.getMemberType());
    }

    private Specification<Member> createSpecification(
            MemberSearchCondition condition,
            String username,
            boolean admin) {

        Specification<Member> specification = Specification.unrestricted();

        // USERの場合だけ所有者条件を追加
        if (!admin) {
            specification = specification.and(
                    (root, query, cb) -> cb.equal(
                            root.get("owner").get("username"),
                            username));
        }

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