package com.github.airmoment.interest.exception;

import org.springframework.http.HttpStatus;

import com.github.airmoment.global.response.base.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterestErrorCode implements BaseCode {
	/*
	400 BAD REQUEST
	 */
	INTEREST_ALREADY_BOOKMARKED(HttpStatus.BAD_REQUEST, "이미 관심노선 설정이 되어있습니다."),
	INTEREST_ALREADY_UNBOOKMARKED(HttpStatus.BAD_REQUEST, "이미 관심노선 설정이 해제되어있습니다."),
	EMAIL_NOTIFICATION_ALREADY_ENABLED(HttpStatus.BAD_REQUEST, "이미 이메일 수신 설정이 되어있습니다."),
	EMAIL_NOTIFICATION_ALREADY_DISABLED(HttpStatus.BAD_REQUEST, "이미 이메일 수신 설정이 해제되어있습니다."),

	/*
	403 FORBIDDEN
	 */
	ACCESS_DENIED(HttpStatus.FORBIDDEN, "자신의 interest에만 접근할 수 있습니다."),

	/*
	404 NOT FOUND
	 */
	INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "interest를 찾을 수 없습니다."),
	;

	private final HttpStatus httpStatus;
	private final String message;
}
