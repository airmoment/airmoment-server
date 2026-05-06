package com.github.airmoment.interest.exception;

import org.springframework.http.HttpStatus;

import com.github.airmoment.global.response.base.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InterestSuccessCode implements BaseCode {

	/*
	200 OK
	 */
	BOOKMARK_CREATED(HttpStatus.OK, "관심 노선 설정이 완료되었습니다."),
	BOOKMARK_DELETED(HttpStatus.OK, "관심 노선 해제가 완료되었습니다." ),
	;

	private final HttpStatus httpStatus;
	private final String message;
}
