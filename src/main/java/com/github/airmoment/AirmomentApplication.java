package com.github.airmoment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.github.airmoment.global.client.serpapi.SerpApiProperties;

@EnableConfigurationProperties(SerpApiProperties.class)
@SpringBootApplication
public class AirmomentApplication {
	public static void main(String[] args) {
		SpringApplication.run(AirmomentApplication.class, args);
	}

}
