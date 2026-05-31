package com.github.airmoment.global.client.eia;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "eia")
public record EiaProperties(
	String apiKey,
	String baseUrl
) {
}
