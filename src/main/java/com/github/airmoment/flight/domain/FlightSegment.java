package com.github.airmoment.flight.domain;

import java.time.LocalDateTime;

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
@Table(name = "flight_segment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlightSegment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Integer segmentOrder;

	@Column(nullable = false, length = 10)
	private String departureAirportCode;

	@Column(nullable = false)
	private String departureAirportName;

	@Column(nullable = false)
	private LocalDateTime departureTime;

	@Column(nullable = false, length = 10)
	private String arrivalAirportCode;

	@Column(nullable = false)
	private String arrivalAirportName;

	@Column(nullable = false)
	private LocalDateTime arrivalTime;

	@Column(nullable = false)
	private Integer duration;

	@Column(nullable = true)
	private String airline;

	@Column(nullable = true)
	private String flightNumber;

	@Column(nullable = true)
	private String travelClass;

	@Column(nullable = true)
	private String legroom;

	@Column(nullable = true)
	private String airplane;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flight_offer_id", nullable = false)
	private FlightOffer flightOffer;

	public static FlightSegment of(FlightOffer flightOffer, Integer segmentOrder,
		String departureAirportCode, String departureAirportName,
		LocalDateTime departureTime, String arrivalAirportCode,
		String arrivalAirportName, LocalDateTime arrivalTime,
		Integer duration, String airline, String flightNumber,
		String travelClass, String legroom, String airplane) {
		FlightSegment segment = new FlightSegment();
		segment.flightOffer = flightOffer;
		segment.segmentOrder = segmentOrder;
		segment.departureAirportCode = departureAirportCode;
		segment.departureAirportName = departureAirportName;
		segment.departureTime = departureTime;
		segment.arrivalAirportCode = arrivalAirportCode;
		segment.arrivalAirportName = arrivalAirportName;
		segment.arrivalTime = arrivalTime;
		segment.duration = duration;
		segment.airline = airline;
		segment.flightNumber = flightNumber;
		segment.travelClass = travelClass;
		segment.legroom = legroom;
		segment.airplane = airplane;
		return segment;
	}
}
