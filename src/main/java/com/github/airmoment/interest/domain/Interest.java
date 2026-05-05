package com.github.airmoment.interest.domain;

import java.time.LocalDate;

import com.github.airmoment.flight.domain.enums.AirportCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interest")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Interest {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private AirportCode departureCode;

	@Column(nullable = false)
	private AirportCode arrivalCode;

	@Column(nullable = false)
	private LocalDate departureAt;

	@Column(nullable = false)
	private boolean isBookmarked;

	@Column(nullable = false)
	private boolean isEmailNotificationEnabled;

	public static Interest of(AirportCode departureCode, AirportCode arrivalCode, LocalDate departureAt, boolean isBookmarked, boolean isEmailNotificationEnabled) {
		Interest interest = new Interest();
		interest.departureCode = departureCode;
		interest.arrivalCode = arrivalCode;
		interest.departureAt = departureAt;
		interest.isBookmarked = isBookmarked;
		interest.isEmailNotificationEnabled = isEmailNotificationEnabled;
		return interest;
	}
}
