package com.github.airmoment.flight.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "holiday")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Holiday {

	@Id
	private LocalDate date;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	private String name;

	public static Holiday of(LocalDate date, String name) {
		Holiday holiday = new Holiday();
		holiday.date = date;
		holiday.name = name;
		holiday.createdAt = LocalDateTime.now();
		return holiday;
	}
}
