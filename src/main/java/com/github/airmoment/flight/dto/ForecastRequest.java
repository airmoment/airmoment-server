package com.github.airmoment.flight.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ForecastRequest(
	String routeId,
	Integer currentCheapestPrice,
	Integer daysToDeparture,
	Integer outboundMonth,
	String searchedDayOfWeek,
	String outboundDayOfWeek,
	Integer isWeekendSearch,
	Integer isPeakSeason,
	Integer isHolidayNear,
	Integer isLongHaul,
	Integer offerCount,
	Float nonstopRatio,
	Integer cheapestNonstopPrice,
	Integer cheapestOfferHasLayover,
	String priceLevel,
	Integer currGapToTypicalMin,
	Integer currGapToTypicalMax,
	Float histRecentStd,
	Float histRecentSlope,
	Float currVsHistMean,
	Integer lag1Price,
	Integer priceChange1,
	Float rollingStd3,
	Integer priceVsRollingMean3,
	Float oilPriceUsd,
	Float oilChange7d,
	Float arrFxChange7d
) {
}
