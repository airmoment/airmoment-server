package com.github.airmoment.flight.dto;

import java.util.List;

public record FlightListResponse(
	int totalCount,
	FlightPredictDto predict,
	GraphResponse priceForecast,
	List<FlightItemResponse> flightList
) {
	public static FlightListResponse of(FlightPredictDto predict, List<FlightItemResponse> items) {
		return new FlightListResponse(items != null ? items.size() : 0, predict, null, items);
	}

	public static FlightListResponse of(FlightPredictDto predict, List<FlightItemResponse> items, GraphResponse priceForecast) {
		return new FlightListResponse(items != null ? items.size() : 0, predict, priceForecast, items);
	}
}
