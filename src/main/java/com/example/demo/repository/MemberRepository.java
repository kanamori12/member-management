package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.demo.model.Member;

public interface MemberRepository
        extends JpaRepository<Member, Long>,
                JpaSpecificationExecutor<Member> {

    List<Member> findByOwnerUsername(String username);

    Optional<Member> findByIdAndOwnerUsername(
            Long id,
            String username);
        
    Page<Member> findByOwnerUsername(
        String username,
        Pageable pageable);
}