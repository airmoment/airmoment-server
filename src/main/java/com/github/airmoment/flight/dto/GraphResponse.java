package com.github.airmoment.flight.dto;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public record GraphResponse(
	String route,
	int daysUntilDeparture,
	int currentPrice,
	List<PredictionPoint> predictions,
	ZonedDateTime predictedAt
) {
	public static GraphResponse from(String routeId, int daysToDeparture, ForecastDto dto) {
		List<PredictionPoint> points = new ArrayList<>();
		for (int i = 0; i < dto.x().size(); i++) {
			points.add(new PredictionPoint(
				dto.x().get(i),
				dto.q10().get(i),
				dto.q25().get(i),
				dto.q50().get(i),
				dto.q75().get(i),
				dto.q90().get(i)
			));
		}
		return new GraphResponse(routeId, daysToDeparture, dto.currentPrice(), points, ZonedDateTime.now());
	}
}
