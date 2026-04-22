package com.github.airmoment.global.client.fastapi;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.github.airmoment.flight.dto.AIPredictionResponse;
import com.github.airmoment.flight.dto.FlightFeatureVector;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AIServerClient {

	private final RestClient restClient;
	private final AIServerProperties aiServerProperties;

	public AIPredictionResponse predict(FlightFeatureVector featureVector) {
		return restClient.post()
			.uri(aiServerProperties.baseUrl() + "/predict")
			.body(featureVector)
			.retrieve()
			.body(AIPredictionResponse.class);
	}
}
