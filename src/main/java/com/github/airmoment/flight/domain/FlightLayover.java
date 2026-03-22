package com.github.airmoment.flight.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flight_layover")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlightLayover {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Integer layoverOrder;

	@Column(nullable = false, length = 10)
	private String airportCode;

	@Column(nullable = false)
	private String airportName;

	@Column(nullable = false)
	private Integer duration;

	@Column(nullable = false)
	private Boolean isOvernight;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flight_offer_id", nullable = false)
	private FlightOffer flightOffer;

	public static FlightLayover of(FlightOffer flightOffer, Integer layoverOrder,
		String airportCode, String airportName,
		Integer duration, Boolean isOvernight) {
		FlightLayover layover = new FlightLayover();
		layover.flightOffer = flightOffer;
		layover.layoverOrder = layoverOrder;
		layover.airportCode = airportCode;
		layover.airportName = airportName;
		layover.duration = duration;
		layover.isOvernight = isOvernight;
		return layover;
	}
}
