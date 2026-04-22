package com.github.airmoment.flight.dto;

public record FlightFeatureVector(
	String routeId,
	String searchedDayOfWeek,
	int daysToDeparture,
	boolean isWeekendSearch,
	boolean isLongHaul,
	int offerCount,
	float nonstopRatio,
	Integer cheapestNonstopPrice,
	boolean cheapestOfferHasLayover,
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
