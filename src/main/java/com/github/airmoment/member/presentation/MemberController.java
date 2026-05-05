package com.github.airmoment.member.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.airmoment.global.response.dto.SuccessResponse;
import com.github.airmoment.member.dto.LoginRequest;
import com.github.airmoment.member.dto.RefreshRequest;
import com.github.airmoment.member.dto.SignupRequest;
import com.github.airmoment.member.dto.TokenResponse;
import com.github.airmoment.member.exception.MemberSuccessCode;
import com.github.airmoment.member.service.MemberService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

	private final MemberService memberService;

	@PostMapping("/signup")
	public ResponseEntity<SuccessResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
		memberService.signup(request);
		return ResponseEntity
			.status(MemberSuccessCode.SIGNUP_SUCCESS.getHttpStatus())
			.body(SuccessResponse.of(MemberSuccessCode.SIGNUP_SUCCESS));
	}

	@PostMapping("/login")
	public ResponseEntity<SuccessResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
		TokenResponse tokens = memberService.login(request);
		return ResponseEntity.ok(SuccessResponse.of(MemberSuccessCode.LOGIN_SUCCESS, tokens));
	}

	@PostMapping("/refresh")
	public ResponseEntity<SuccessResponse<TokenResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
		TokenResponse tokens = memberService.refresh(request);
		return ResponseEntity.ok(SuccessResponse.of(MemberSuccessCode.REFRESH_SUCCESS, tokens));
	}
}
