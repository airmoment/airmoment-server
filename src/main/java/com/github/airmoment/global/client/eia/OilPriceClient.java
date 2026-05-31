package com.github.airmoment.global.client.eia;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.github.airmoment.flight.dto.DailyOilPrice;
import com.github.airmoment.global.client.eia.dto.EiaResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OilPriceClient {
	private final RestClient restClient;
	private final EiaProperties properties;

	@Value("${eia.api-key}") String apiKey;

	public List<DailyOilPrice> fetchWtiRange(LocalDate start, LocalDate end) {
		EiaResponse res = restClient.get()
			.uri(uri -> uri.path(properties.baseUrl())
				.queryParam("api_key", properties.apiKey())
				.queryParam("start", start)
				.queryParam("end", end)
				.build())
			.retrieve()
			.body(EiaResponse.class);

		return res != null ? res.toDailyPrices() : List.of();
	}
}