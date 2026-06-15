package com.github.airmoment.flight.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExplainResponse(
	Integer q50,
	String direction,
	List<String> reasons
) {
}
