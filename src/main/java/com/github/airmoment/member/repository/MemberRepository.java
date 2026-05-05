package com.github.airmoment.member.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.airmoment.member.domain.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}
