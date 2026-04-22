package com.github.airmoment.flight.domain;

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
@Table(name = "airline")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Airline {

	private static final String DEFAULT_PHOTO =
		"https://www.logoyogo.com/web/wp-content/uploads/edd/2021/03/logoyogo-1-164.jpg";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true)
	private String name;

	@Column(nullable = false)
	private String photo;

	public static Airline of(String name) {
		Airline airline = new Airline();
		airline.name = name;
		airline.photo = DEFAULT_PHOTO;
		return airline;
	}
}
