package com.github.airmoment.interest.dto;

import java.time.LocalDate;

import com.github.airmoment.flight.domain.enums.AirportCode;
import com.github.airmoment.interest.domain.Interest;

public record InterestDto(
	AirportCode departureCode,
	AirportCode arrivalCode,
	LocalDate departureAt,
	boolean nonstopOnly,
	boolean isBookmarked,
	boolean isEmailNotificationEnabled
) {
	public static InterestDto from(Interest interest) {
		return new InterestDto(
			interest.getDepartureCode(),
			interest.getArrivalCode(),
			interest.getDepartureAt(),
			interest.isNonstopOnly(),
			Boolean.TRUE.equals(interest.getIsBookmarked()),
			Boolean.TRUE.equals(interest.getIsEmailNotificationEnabled())
		);
	}
}
