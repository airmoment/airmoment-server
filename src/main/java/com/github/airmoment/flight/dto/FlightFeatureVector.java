package com.github.airmoment.flight.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.NotNull;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record FlightFeatureVector(
	@NotNull
	String routeId,
	@NotNull
	String departureAirportCode,
	@NotNull
	String arrivalAirportCode,
	String outboundDate,
	@NotNull
	String searchedDayOfWeek,
	@NotNull
	int daysToDeparture,
	@NotNull
	boolean isWeekendSearch,
	@NotNull
	boolean isLongHaul,
	@NotNull
	int offerCount,
	@NotNull
	float nonstopRatio,
	Integer cheapestNonstopPrice,
	@NotNull
	boolean cheapestOfferHasLayover,
	@NotNull
	int currentCheapestPrice,
	Integer currGapToTypicalMin,
	Integer currGapToTypicalMax,
	Float histRecentStd,
	Float histRecentSlope,
	Float currVsHistMean,
	Float priceChange1,
	Float rollingStd3,
	Float priceVsRollingMean3
) {
}
