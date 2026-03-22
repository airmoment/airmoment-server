package com.github.airmoment.global.client.serpapi.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FlightOfferDto(
	@JsonProperty("flights") List<FlightSegmentDto> flights,
	@JsonProperty("layovers") List<LayoverDto> layovers,
	@JsonProperty("total_duration") Integer totalDuration,
	@JsonProperty("price") Integer price,
	@JsonProperty("type") String type,
	@JsonProperty("departure_token") String departureToken
) {}