package com.github.airmoment.interest.dto;

import java.time.LocalDate;

import com.github.airmoment.flight.domain.enums.AirportCode;

public record BookmarkDto(
	AirportCode departureCode,
	AirportCode arrivalCode,
	LocalDate departureAt,
	boolean nonstopOnly,
	boolean isBookmarked,
	boolean isEmailNotificationEnabled
) {

	public static BookmarkDto from(
		AirportCode departureCode,
		AirportCode arrivalCode,
		LocalDate departureAt,
		boolean nonstopOnly,
		boolean isBookmarked,
		boolean isEmailNotificationEnabled
	) {
		return new BookmarkDto(departureCode, arrivalCode, departureAt, nonstopOnly, isBookmarked, isEmailNotificationEnabled);
	}
}
