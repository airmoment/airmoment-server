package com.github.airmoment.global.client.serpapi.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PriceInsightDto(
	@JsonProperty("lowest_price") Integer lowestPrice,
	@JsonProperty("price_level") String priceLevel,
	@JsonProperty("typical_price_range") List<Integer> typicalPriceRange,
	@JsonProperty("price_history") List<List<Long>> priceHistory
) {}