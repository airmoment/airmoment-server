package com.github.airmoment.flight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
	@JsonProperty("lag_1_price") Integer lag1Price,
	@JsonProperty("price_change_1") Integer priceChange1,
	@JsonProperty("rolling_std_3") Float rollingStd3,
	@JsonProperty("price_vs_rolling_mean_3") Integer priceVsRollingMean3,
	Float oilPriceUsd,
	@JsonProperty("oil_change_7d") Float oilChange7d,
	@JsonProperty("arr_fx_change_7d") Float arrFxChange7d
) {
}
