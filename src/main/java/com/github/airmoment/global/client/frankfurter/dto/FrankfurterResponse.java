package com.github.airmoment.global.client.frankfurter.dto;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FrankfurterResponse(String date, Map<String, Double> rates) {

	public Double rate(String currency) {
		return rates != null ? rates.get(currency) : null;
	}
}
