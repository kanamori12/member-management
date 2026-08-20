package com.example.demo.service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.exception.MemberNotFoundException;
import com.example.demo.model.Member;
import com.example.demo.model.MemberSearchCondition;
import com.example.demo.model.Tag;
import com.example.demo.repository.MemberRepository;

@Service
public class MemberServiceImpl implements MemberService {

        private final MemberRepository memberRepository;
        private final TagService tagService;

        public MemberServiceImpl(
                        MemberRepository memberRepository,
                        TagService tagService) {

                this.memberRepository = memberRepository;
                this.tagService = tagService;
        }

        // =========================
        // 会員登録
        // =========================

        @Override
        @Transactional
        public void register(Member member) {

                setTags(member);

                memberRepository.save(member);
        }

        // =========================
        // 会員一覧
        // =========================

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

        // =========================
        // 会員検索
        // =========================

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

                Sort sort = createSort(
                                condition.getSort());

                Pageable sortedPageable = PageRequest.of(
                                pageable.getPageNumber(),
                                pageable.getPageSize(),
                                sort);

                return memberRepository.findAll(
                                specification,
                                sortedPageable);
        }

        // =========================
        // 会員削除
        // =========================

        @Override
        public void delete(
                        Long id,
                        String username,
                        boolean admin) {

                Member member = findById(
                                id,
                                username,
                                admin);

                memberRepository.delete(member);
        }

        // =========================
        // IDから会員取得
        // =========================

        @Override
        public Member findById(
                        Long id,
                        String username,
                        boolean admin) {

                if (admin) {

                        return memberRepository
                                        .findById(id)
                                        .orElseThrow(() -> new MemberNotFoundException(id));
                }

                return memberRepository
                                .findByIdAndOwnerUsername(
                                                id,
                                                username)
                                .orElseThrow(() -> new MemberNotFoundException(id));
        }

        // =========================
        // 会員更新
        // =========================

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

                existingMember.setName(
                                member.getName());

                existingMember.setAge(
                                member.getAge());

                existingMember.setMemberType(
                                member.getMemberType());

                existingMember.setTagNames(
                                member.getTagNames());

                if (member.getProfileImageName() != null) {
                        existingMember.setProfileImageName(
                                        member.getProfileImageName());
                }

                setTags(existingMember);
        }

        // =========================
        // タグ設定
        // =========================

        private void setTags(Member member) {

                if (member.getTagNames() == null
                                || member.getTagNames().isBlank()) {

                        member.getTags().clear();

                        return;
                }

                Set<Tag> tags = Arrays.stream(
                                member.getTagNames()
                                                .split(","))
                                .map(String::trim)
                                .filter(name -> !name.isBlank())
                                .distinct()
                                .map(tagService::findOrCreate)
                                .collect(Collectors.toSet());

                member.setTags(tags);
        }

        // =========================
        // 検索条件作成
        // =========================

        private Specification<Member> createSpecification(
                        MemberSearchCondition condition,
                        String username,
                        boolean admin) {

                Specification<Member> specification = Specification.unrestricted();

                // USERの場合だけ所有者条件を追加
                if (!admin) {

                        specification = specification.and(
                                        (root, query, cb) -> cb.equal(
                                                        root.get("owner")
                                                                        .get("username"),
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
                                        (root, query, cb) -> cb.equal(
                                                        root.get("age"),
                                                        age));
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

                // タグ
                if (condition.getTagName() != null
                                && !condition.getTagName().isBlank()) {

                        String tagName = condition.getTagName().trim();

                        specification = specification.and(
                                        (root, query, cb) -> {

                                                var tags = root.join("tags");

                                                return cb.equal(
                                                                tags.get("name"),
                                                                tagName);
                                        });
                }

                return specification;
        }

        // =========================
        // 並び順
        // =========================

        private Sort createSort(String sort) {

                if ("idDesc".equals(sort)) {

                        return Sort.by(
                                        Sort.Direction.DESC,
                                        "id");
                }

                if ("nameAsc".equals(sort)) {

                        return Sort.by(
                                        Sort.Direction.ASC,
                                        "name");
                }

                if ("ageAsc".equals(sort)) {

                        return Sort.by(
                                        Sort.Direction.ASC,
                                        "age");
                }

                // idAsc または値が不明な場合
                return Sort.by(
                                Sort.Direction.ASC,
                                "id");
        }
}