package com.github.airmoment.flight.dto;

import java.util.List;

public record FlightListResponse(
	int totalCount,
	List<FlightItemResponse> flightList
) {
	public static FlightListResponse of(List<FlightItemResponse> items) {
		return new FlightListResponse(items.size(), items);
	}
}
