package com.github.airmoment.global.client.serpapi;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.github.airmoment.global.client.serpapi.dto.FlightSearchResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SerpApiClient {

	private final RestClient restClient;
	private final SerpApiProperties properties;

	public FlightSearchResponse fetchFlights(String departureId, String arrivalId,
		String outboundDate) {
		return restClient.get()
			.uri(uriBuilder -> uriBuilder
				.scheme("https")
				.host("serpapi.com")
				.path("/search")
				.queryParam("engine", "google_flights")
				.queryParam("api_key", properties.apiKey())
				.queryParam("departure_id", departureId)
				.queryParam("arrival_id", arrivalId)
				.queryParam("outbound_date", outboundDate)
				.queryParam("type", "2")       // ← 편도
				.queryParam("currency", "KRW")
				.queryParam("hl", "ko")
				.queryParam("gl", "kr")
				.queryParam("travel_class", "1")
				.build())
			.retrieve()
			.body(FlightSearchResponse.class);
	}
}