package com.example.demo.service;

import java.util.Comparator;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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

        Specification<Member> specification = Specification.unrestricted();

        // 名前
        if (condition.getName() != null
                && !condition.getName().isBlank()) {

            String name = condition.getName().trim();

            specification = specification.and(
                    (root, query, cb) -> cb.like(root.get("name"), "%" + name + "%"));
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
                    (root, query, cb) -> cb.equal(root.get("memberType"), memberType));
        }

        List<Member> result = memberRepository.findAll(specification);

        // 並び順
        if ("idAsc".equals(condition.getSort())) {

            result.sort(Comparator.comparing(Member::getId));

        } else if ("idDesc".equals(condition.getSort())) {

            result.sort(
                    Comparator.comparing(Member::getId).reversed());

        } else if ("nameAsc".equals(condition.getSort())) {

            result.sort(Comparator.comparing(Member::getName));

        } else if ("ageAsc".equals(condition.getSort())) {

            result.sort(Comparator.comparing(Member::getAge));
        }

        return result;
    }

    @Override
    public void delete(Long id) {

        memberRepository.deleteById(id);
    }

    @Override
    public Member findById(Long id) {

        return memberRepository.findById(id).orElse(null);
    }

    @Override
    public void update(Member member) {

        memberRepository.save(member);
    }

}