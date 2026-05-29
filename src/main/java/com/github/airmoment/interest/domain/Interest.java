package com.github.airmoment.interest.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.github.airmoment.flight.domain.enums.AirportCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
	@Enumerated(EnumType.STRING)
	private AirportCode departureCode;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AirportCode arrivalCode;

	@Column(nullable = false)
	private LocalDate departureAt;

	@Column(nullable = false)
	private boolean nonstopOnly;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private Boolean isBookmarked = false;

	@Column(nullable = false)
	private Boolean isEmailNotificationEnabled = false;

	@Column(nullable = false)
	private Long memberId;

	public static Interest of(
		AirportCode departureCode,
		AirportCode arrivalCode,
		LocalDate departureAt,
		boolean nonstopOnly,
		Long memberId) {
		Interest interest = new Interest();
		interest.departureCode = departureCode;
		interest.arrivalCode = arrivalCode;
		interest.departureAt = departureAt;
		interest.nonstopOnly = nonstopOnly;
		interest.createdAt = LocalDateTime.now();
		interest.memberId = memberId;
		return interest;
	}

	public void updateAsBookmarked() {
		this.isBookmarked = true;
	}

	public void updateAsEmailNotificationEnabled() {
		this.isEmailNotificationEnabled = true;
	}

	public void updateAsUnbookmarked() {
		this.isBookmarked = false;
	}

	public void updateAsEmailNotificationDisabled() {
		this.isEmailNotificationEnabled = false;
	}
}
