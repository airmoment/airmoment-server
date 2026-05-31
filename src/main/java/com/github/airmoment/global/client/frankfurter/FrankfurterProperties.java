package com.github.airmoment.global.client.frankfurter;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "frankfurter")
public record FrankfurterProperties(String baseUrl) {
}
