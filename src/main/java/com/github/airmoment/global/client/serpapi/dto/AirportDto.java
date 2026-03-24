package com.github.airmoment.global.client.serpapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AirportDto(
	@JsonProperty("name") String name,
	@JsonProperty("id") String id,
	@JsonProperty("time") String time
) {
}