package com.github.airmoment.global.client.serpapi.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FlightSearchResponse(
	@JsonProperty("best_flights") List<FlightOfferDto> bestFlights,
	@JsonProperty("other_flights") List<FlightOfferDto> otherFlights,
	@JsonProperty("price_insights") PriceInsightDto priceInsights
) {
}
