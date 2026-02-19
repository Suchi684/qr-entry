package com.example.demo.qr.Repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.qr.Entity.Member;

public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
}
