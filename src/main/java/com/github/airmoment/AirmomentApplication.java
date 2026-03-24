package com.github.airmoment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.github.airmoment.global.client.discord.DiscordProperties;
import com.github.airmoment.global.client.google.GoogleSheetsProperties;
import com.github.airmoment.global.client.serpapi.SerpApiProperties;

@EnableConfigurationProperties({
	SerpApiProperties.class,
	DiscordProperties.class,
	GoogleSheetsProperties.class
})
@SpringBootApplication
public class AirmomentApplication {
	public static void main(String[] args) {
		SpringApplication.run(AirmomentApplication.class, args);
	}

}
