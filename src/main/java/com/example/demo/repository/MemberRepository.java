package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.demo.model.Member;

public interface MemberRepository
        extends JpaRepository<Member, Long>,
                JpaSpecificationExecutor<Member> {

    // =========================
    // USER用：一覧取得
    // タグも同時取得
    // =========================

    @EntityGraph(attributePaths = "tags")
    Page<Member> findByOwnerUsername(
            String username,
            Pageable pageable);


    // =========================
    // ADMIN用：一覧取得
    // JpaRepositoryのfindAllを再定義し、
    // タグも同時取得
    // =========================

    @Override
    @EntityGraph(attributePaths = "tags")
    Page<Member> findAll(
            Pageable pageable);


    // =========================
    // USER用：ID + 所有者検索
    // =========================

    Optional<Member> findByIdAndOwnerUsername(
            Long id,
            String username);


    // =========================
    // USER用：所有者検索
    // =========================

    List<Member> findByOwnerUsername(
            String username);
}