package com.github.airmoment.flight.dto;

import java.time.LocalTime;

public record FlightItemResponse(
	String airlineName,
	String airlinePhoto,
	LocalTime departureTime,
	LocalTime arrivalTime,
	int duration,
	int price
) {
	public static FlightItemResponse from(CachedFlightItem item) {
		return new FlightItemResponse(
			item.airlineName(),
			item.airlinePhoto(),
			item.departureTime(),
			item.arrivalTime(),
			item.duration(),
			item.price()
		);
	}
}
