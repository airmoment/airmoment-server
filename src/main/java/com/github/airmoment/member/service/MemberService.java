package com.github.airmoment.member.service;

import java.time.Duration;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.airmoment.global.exception.AirmomentException;
import com.github.airmoment.global.jwt.JwtProperties;
import com.github.airmoment.global.jwt.JwtProvider;
import com.github.airmoment.member.domain.Member;
import com.github.airmoment.member.dto.LoginRequest;
import com.github.airmoment.member.dto.RefreshRequest;
import com.github.airmoment.member.dto.SignupRequest;
import com.github.airmoment.member.dto.TokenResponse;
import com.github.airmoment.member.exception.MemberErrorCode;
import com.github.airmoment.member.repository.MemberRepository;
import com.github.airmoment.member.repository.RefreshTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final JwtProperties jwtProperties;

	@Transactional
	public void signup(SignupRequest request) {
		if (memberRepository.existsByEmail(request.email())) {
			throw new AirmomentException(MemberErrorCode.DUPLICATE_EMAIL);
		}

		Member member = Member.of(request.email(), passwordEncoder.encode(request.password()), request.name());
		memberRepository.save(member);
	}

	@Transactional(readOnly = true)
	public TokenResponse login(LoginRequest request) {
		Member member = memberRepository.findByEmail(request.email())
			.orElseThrow(() -> new AirmomentException(MemberErrorCode.INVALID_CREDENTIALS));

		if (!passwordEncoder.matches(request.password(), member.getPassword())) {
			throw new AirmomentException(MemberErrorCode.INVALID_CREDENTIALS);
		}

		return issueTokens(member.getId());
	}

	public TokenResponse refresh(RefreshRequest request) {
		String refreshToken = request.refreshToken();

		if (!jwtProvider.validate(refreshToken)) {
			throw new AirmomentException(MemberErrorCode.INVALID_REFRESH_TOKEN);
		}

		Long memberId = jwtProvider.getMemberId(refreshToken);
		String stored = refreshTokenRepository.find(memberId);

		if (!refreshToken.equals(stored)) {
			throw new AirmomentException(MemberErrorCode.INVALID_REFRESH_TOKEN);
		}

		return issueTokens(memberId);
	}

	private TokenResponse issueTokens(Long memberId) {
		String accessToken = jwtProvider.createAccessToken(memberId);
		String refreshToken = jwtProvider.createRefreshToken(memberId);
		refreshTokenRepository.save(memberId, refreshToken,
			Duration.ofMillis(jwtProperties.refreshTokenExpiry()));
		return new TokenResponse(accessToken, refreshToken);
	}
}
