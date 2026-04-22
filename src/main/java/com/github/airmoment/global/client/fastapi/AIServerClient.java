package com.github.airmoment.global.client.fastapi;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.github.airmoment.flight.dto.AIPredictionResponse;
import com.github.airmoment.flight.dto.FlightFeatureVector;

@Component
public class AIServerClient {

	private final RestClient restClient;
	private final AIServerProperties aiServerProperties;

	public AIServerClient(
		@Qualifier("aiServerRestClient") RestClient restClient,
		AIServerProperties aiServerProperties
	) {
		this.restClient = restClient;
		this.aiServerProperties = aiServerProperties;
	}

	public AIPredictionResponse predict(FlightFeatureVector featureVector) {
		return restClient.post()
			.uri(aiServerProperties.baseUrl() + "/predict")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.APPLICATION_JSON)
			.body(featureVector)
			.retrieve()
			.body(AIPredictionResponse.class);
	}
}
