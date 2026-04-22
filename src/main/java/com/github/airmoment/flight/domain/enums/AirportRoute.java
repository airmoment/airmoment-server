package com.github.airmoment.flight.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AirportRoute {
	ICN_TO_CDG(AirportCode.ICN, AirportCode.CDG),
	ICN_TO_JFK(AirportCode.ICN, AirportCode.JFK),
	ICN_TO_SYD(AirportCode.ICN, AirportCode.SYD);

	private final AirportCode departure;
	private final AirportCode arrival;
}
