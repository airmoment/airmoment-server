package com.github.airmoment.member.repository;

import java.time.Duration;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

	private static final String KEY_PREFIX = "refresh:";

	private final RedisTemplate<String, String> redisTemplate;

	public void save(Long memberId, String refreshToken, Duration ttl) {
		redisTemplate.opsForValue().set(KEY_PREFIX + memberId, refreshToken, ttl);
	}

	public String find(Long memberId) {
		return redisTemplate.opsForValue().get(KEY_PREFIX + memberId);
	}

	public void delete(Long memberId) {
		redisTemplate.delete(KEY_PREFIX + memberId);
	}
}
