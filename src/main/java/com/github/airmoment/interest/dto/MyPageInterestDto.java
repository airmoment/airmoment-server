package com.github.airmoment.interest.dto;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.airmoment.flight.dto.GraphResponse;
import com.github.airmoment.flight.dto.PredictionPoint;

public record MyPageInterestDto(
	Long interestId,
	String departureCode,
	String arrivalCode,
	LocalDate departureAt,
	String departureDayOfWeek,
	boolean nonStopOnly,
	boolean isBookmarked,
	boolean isEmailNotificationEnabled,
	List<PredictionPoint> predictions,
	ZonedDateTime predictedAt
) {
	public static MyPageInterestDto of(InterestDto interest, GraphResponse graphResponse) {
		return new MyPageInterestDto(
			interest.interestId(),
			interest.departureCode().name(),
			interest.arrivalCode().name(),
			interest.departureAt(),
			toKoreanDayOfWeek(interest.departureAt().getDayOfWeek()),
			interest.nonstopOnly(),
			interest.isBookmarked(),
			interest.isEmailNotificationEnabled(),
			graphResponse != null ? graphResponse.predictions() : null,
			graphResponse != null ? graphResponse.predictedAt() : null
		);
	}

	private static String toKoreanDayOfWeek(DayOfWeek dayOfWeek) {
		return switch (dayOfWeek) {
			case MONDAY -> "월";
			case TUESDAY -> "화";
			case WEDNESDAY -> "수";
			case THURSDAY -> "목";
			case FRIDAY -> "금";
			case SATURDAY -> "토";
			case SUNDAY -> "일";
		};
	}
}
