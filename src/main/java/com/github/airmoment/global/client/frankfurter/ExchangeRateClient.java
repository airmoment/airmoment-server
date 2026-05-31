package com.github.airmoment.global.client.frankfurter;

import java.time.LocalDate;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.github.airmoment.global.client.frankfurter.dto.FrankfurterResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExchangeRateClient {

	private static final String FROM = "USD";
	private static final String TO = "KRW";

	private final RestClient restClient;
	private final FrankfurterProperties properties;

	public FrankfurterResponse fetchLatest() {
		return fetchLatest(FROM);
	}

	public FrankfurterResponse fetchLatest(String from) {
		return restClient.get()
			.uri(properties.baseUrl() + "/latest?from={from}&to={to}", from, TO)
			.retrieve()
			.body(FrankfurterResponse.class);
	}

	public FrankfurterResponse fetchByDate(LocalDate date) {
		return fetchByDate(date, FROM);
	}

	public FrankfurterResponse fetchByDate(LocalDate date, String from) {
		return restClient.get()
			.uri(properties.baseUrl() + "/{date}?from={from}&to={to}", date, from, TO)
			.retrieve()
			.body(FrankfurterResponse.class);
	}
}
