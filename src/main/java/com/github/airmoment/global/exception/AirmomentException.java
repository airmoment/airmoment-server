package com.github.airmoment.global.exception;

import com.github.airmoment.global.response.base.BaseCode;

import lombok.Getter;

@Getter
public class AirmomentException extends RuntimeException {
	private final BaseCode baseCode;

	public AirmomentException(BaseCode baseCode) {
		super(baseCode.getMessage());
		this.baseCode = baseCode;
	}
}
