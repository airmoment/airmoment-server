package com.github.airmoment.global.config;

import java.time.Duration;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

	@Bean
	public RestClient restClient() {
		return RestClient.create();
	}

	@Bean
	public RestClient aiServerRestClient() {
		return RestClient.builder()
			.requestFactory(ClientHttpRequestFactories.get(
				ClientHttpRequestFactorySettings.DEFAULTS
					.withConnectTimeout(Duration.ofSeconds(2))
					.withReadTimeout(Duration.ofSeconds(5))
			))
			.build();
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOriginPatterns("*")
			.allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
			.allowedHeaders("*")
			.allowCredentials(true);
	}

}
