package com.github.airmoment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.github.airmoment.global.client.serpapi.SerpApiProperties;

@EnableScheduling
@EnableConfigurationProperties({
	SerpApiProperties.class
})
@SpringBootApplication
public class AirmomentApplication {
	public static void main(String[] args) {
		SpringApplication.run(AirmomentApplication.class, args);
	}

}
