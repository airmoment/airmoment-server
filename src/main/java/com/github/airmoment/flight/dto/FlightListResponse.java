package com.github.airmoment.flight.dto;

import java.util.List;

public record FlightListResponse(
	FlightPredictDto predict,
	int totalCount,
	List<FlightItemResponse> flightList
) {
	public static FlightListResponse of(FlightPredictDto predict, List<FlightItemResponse> items) {
		return new FlightListResponse(predict, items.size(), items);
	}
}
