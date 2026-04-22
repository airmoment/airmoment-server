package com.github.airmoment.flight.dto;

import java.time.LocalTime;

public record CachedFlightItem(
	String airlineName,
	String airlinePhoto,
	LocalTime departureTime,
	LocalTime arrivalTime,
	int duration,
	int price,
	boolean nonstop
) {
}
