package com.github.airmoment.global.client.serpapi.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FlightSegmentDto(
	@JsonProperty("departure_airport") AirportDto departureAirport,
	@JsonProperty("arrival_airport") AirportDto arrivalAirport,
	@JsonProperty("duration") Integer duration,
	@JsonProperty("airline") String airline,
	@JsonProperty("flight_number") String flightNumber,
	@JsonProperty("travel_class") String travelClass,
	@JsonProperty("legroom") String legroom,
	@JsonProperty("airplane") String airplane
) {
}
