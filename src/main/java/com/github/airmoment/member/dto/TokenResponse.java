package com.github.airmoment.member.dto;

public record TokenResponse(
	String accessToken,
	String refreshToken
) {
}
