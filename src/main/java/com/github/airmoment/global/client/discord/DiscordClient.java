package com.github.airmoment.global.client.discord;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DiscordClient {

	private final RestClient restClient;
	private final DiscordProperties properties;

	public void sendMessage(String message) {
		restClient.post()
			.uri(properties.webhookUrl())
			.contentType(MediaType.APPLICATION_JSON)
			.body(Map.of("content", message))
			.retrieve()
			.toBodilessEntity();

		log.info("Discord 메시지 전송 완료");
	}
}