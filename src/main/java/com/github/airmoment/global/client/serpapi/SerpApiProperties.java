package com.github.airmoment.global.client.serpapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "serpapi")
public record SerpApiProperties(
	String apiKey,
	String baseUrl
) {
}
