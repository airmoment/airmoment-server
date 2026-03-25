package com.github.airmoment.global.client.google;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.sheets")
public record GoogleSheetsProperties(
	String spreadsheetId,
	String credentialsPath
) {
}