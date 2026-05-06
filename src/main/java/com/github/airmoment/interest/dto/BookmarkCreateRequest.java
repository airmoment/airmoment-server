package com.github.airmoment.interest.dto;

import java.time.LocalDate;

import com.github.airmoment.flight.domain.enums.AirportCode;

import jakarta.validation.constraints.NotNull;

public record BookmarkCreateRequest(
	@NotNull
	AirportCode departureCode,
	@NotNull
	AirportCode arrivalCode,
	@NotNull
	LocalDate departureAt,
	@NotNull
	boolean nonstopOnly
) {
}
