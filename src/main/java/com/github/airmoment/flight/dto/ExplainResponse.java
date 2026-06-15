package com.github.airmoment.flight.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExplainResponse(
	String direction,
	@JsonProperty("direction_amount") int directionAmount,
	List<String> reasons
) {
}
