package com.github.airmoment.global.client.eia;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.github.airmoment.flight.dto.DailyOilPrice;
import com.github.airmoment.global.client.eia.dto.EiaResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OilPriceClient {
	private final RestClient restClient;
	private final EiaProperties properties;

	public List<DailyOilPrice> fetchWtiRange(LocalDate start, LocalDate end) {
		String url = UriComponentsBuilder.fromHttpUrl(properties.baseUrl())
			.queryParam("api_key", properties.apiKey())
			.queryParam("start", start)
			.queryParam("end", end)
			.toUriString();

		EiaResponse res = restClient.get()
			.uri(url)
			.retrieve()
			.body(EiaResponse.class);

		return res != null ? res.toDailyPrices() : List.of();
	}
}