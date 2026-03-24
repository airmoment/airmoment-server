package com.github.airmoment.global.client.discord;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "discord")
public record DiscordProperties(
	String webhookUrl
) {
}
