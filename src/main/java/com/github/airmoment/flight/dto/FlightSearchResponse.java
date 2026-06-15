package com.github.airmoment.flight.dto;

import java.util.List;

public record FlightSearchResponse(
	int totalCount,
	FlightPredictDto predict,
	GraphResponse priceForecast,
	ExplainDto explain,
	List<FlightItemResponse> flightList
) {
	public static FlightSearchResponse of(FlightPredictDto predict, List<FlightItemResponse> items) {
		return new FlightSearchResponse(items != null ? items.size() : 0, predict, null, null, items);
	}

	public static FlightSearchResponse of(
		FlightPredictDto predict,
		List<FlightItemResponse> items,
		GraphResponse priceForecast,
		ExplainDto explain
	) {
		return new FlightSearchResponse(items != null ? items.size() : 0, predict, priceForecast, explain, items);
	}
}
