package com.github.airmoment.member.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.github.airmoment.member.domain.Member;
import com.github.airmoment.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String memberId) throws UsernameNotFoundException {
		Member member = memberRepository.findById(Long.parseLong(memberId))
			.orElseThrow(() -> new UsernameNotFoundException("회원을 찾을 수 없습니다: " + memberId));

		return User.builder()
			.username(member.getId().toString())
			.password(member.getPassword())
			.roles("USER")
			.build();
	}
}
