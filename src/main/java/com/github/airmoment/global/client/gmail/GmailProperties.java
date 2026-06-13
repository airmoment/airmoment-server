package com.github.airmoment.global.client.gmail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gmail")
public record GmailProperties(String clientId, String clientSecret, String refreshToken, String senderEmail) {
}
