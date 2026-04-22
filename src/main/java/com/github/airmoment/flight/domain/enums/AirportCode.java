package com.github.airmoment.flight.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AirportCode {
	ICN("ICN"),
	CDG("CDG"),
	JFK("JFK"),
	SYD("SYD");

	private final String code;
}
