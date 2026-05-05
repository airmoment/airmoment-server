package com.github.airmoment.global.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtProvider {

	private final SecretKey secretKey;
	private final long accessTokenExpiry;
	private final long refreshTokenExpiry;

	public JwtProvider(JwtProperties properties) {
		this.secretKey = Keys.hmacShaKeyFor(properties.secret().getBytes(StandardCharsets.UTF_8));
		this.accessTokenExpiry = properties.accessTokenExpiry();
		this.refreshTokenExpiry = properties.refreshTokenExpiry();
	}

	public String createAccessToken(Long memberId) {
		return createToken(memberId, accessTokenExpiry);
	}

	public String createRefreshToken(Long memberId) {
		return createToken(memberId, refreshTokenExpiry);
	}

	public Long getMemberId(String token) {
		return parseClaims(token).get("memberId", Long.class);
	}

	public boolean validate(String token) {
		try {
			parseClaims(token);
			return true;
		} catch (JwtException | IllegalArgumentException e) {
			log.debug("유효하지 않은 JWT: {}", e.getMessage());
			return false;
		}
	}

	private String createToken(Long memberId, long expiry) {
		Date now = new Date();
		return Jwts.builder()
			.claim("memberId", memberId)
			.issuedAt(now)
			.expiration(new Date(now.getTime() + expiry))
			.signWith(secretKey)
			.compact();
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(secretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
