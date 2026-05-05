package com.github.airmoment.member.dto;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
	@NotBlank
	String refreshToken
) {
}
