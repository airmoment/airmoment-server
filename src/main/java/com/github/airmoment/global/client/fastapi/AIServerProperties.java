package com.github.airmoment.global.client.fastapi;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai-server")
public record AIServerProperties(String baseUrl) {}

