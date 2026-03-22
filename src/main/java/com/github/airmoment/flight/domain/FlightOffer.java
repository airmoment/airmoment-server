package com.github.airmoment.flight.domain;

import java.util.ArrayList;
import java.util.List;

import com.github.airmoment.flight.domain.enums.FlightDirection;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flight_offer")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlightOffer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Integer price;

	@Column(nullable = false)
	private Integer totalDuration;

	@Column(nullable = false)
	private Boolean hasLayover;

	@Column(nullable = false)
	private Integer layoverCount;

	@Column(nullable = false)
	private Boolean isBest;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private FlightDirection direction;

	@OneToMany(mappedBy = "flightOffer", cascade = CascadeType.ALL)
	private List<FlightSegment> flightSegments = new ArrayList<>();

	@OneToMany(mappedBy = "flightOffer", cascade = CascadeType.ALL)
	private List<FlightLayover> flightLayovers = new ArrayList<>();

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flight_search_id", nullable = false)
	private FlightSearch flightSearch;

	public static FlightOffer of(FlightSearch flightSearch, Integer price, Integer totalDuration,
		Boolean hasLayover, Integer layoverCount,
		Boolean isBest, FlightDirection direction) {
		FlightOffer flightOffer = new FlightOffer();
		flightOffer.flightSearch = flightSearch;
		flightOffer.price = price;
		flightOffer.totalDuration = totalDuration;
		flightOffer.hasLayover = hasLayover;
		flightOffer.layoverCount = layoverCount;
		flightOffer.isBest = isBest;
		flightOffer.direction = direction;
		return flightOffer;
	}
}
