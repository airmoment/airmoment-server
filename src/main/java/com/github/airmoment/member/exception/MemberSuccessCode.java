package com.github.airmoment.member.exception;

import org.springframework.http.HttpStatus;

import com.github.airmoment.global.response.base.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MemberSuccessCode implements BaseCode {

	SIGNUP_SUCCESS(HttpStatus.CREATED, "회원가입이 완료되었습니다."),
	LOGIN_SUCCESS(HttpStatus.OK, "로그인이 완료되었습니다."),
	REFRESH_SUCCESS(HttpStatus.OK, "토큰이 재발급되었습니다."),
	;

	private final HttpStatus httpStatus;
	private final String message;
}
