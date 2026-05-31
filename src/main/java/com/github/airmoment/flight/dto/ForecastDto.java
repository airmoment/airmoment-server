package com.github.airmoment.flight.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ForecastDto(
	Integer currentPrice,
	List<Integer> x,
	List<Integer> q10,
	List<Integer> q25,
	List<Integer> q50,
	List<Integer> q75,
	List<Integer> q90
) {
}
