package com.github.airmoment.exception;

import org.springframework.http.HttpStatus;

import com.github.airmoment.global.response.base.BaseCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FlightErrorCode implements BaseCode {
	/*
	500 INTERNAL SERVER ERROR
	 */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "예기치 않은 서버 에러가 발생하였습니다."),
	;

	private final HttpStatus httpStatus;
	private final String message;
}
