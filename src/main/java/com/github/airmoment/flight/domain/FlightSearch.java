package com.github.airmoment.flight.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flight_search")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlightSearch {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private LocalDateTime searchedAt;

	@Column(nullable = false, length = 10)
	private String departureAirportCode;

	@Column(nullable = false, length = 10)
	private String arrivalAirportCode;

	@Column(nullable = false)
	private LocalDate outboundDate;

	@Column(nullable = false)
	private LocalDate returnDate;

	@Column(nullable = true, columnDefinition = "TEXT")
	private String departureToken;

	@OneToMany(mappedBy = "flightSearch", cascade = CascadeType.ALL)
	private List<FlightOffer> flightOffers = new ArrayList<>();

	@OneToOne(mappedBy = "flightSearch", cascade = CascadeType.ALL)
	private FlightPriceInsight flightPriceInsight;

	@OneToMany(mappedBy = "flightSearch", cascade = CascadeType.ALL)
	private List<FlightPriceHistory> flightPriceHistories = new ArrayList<>();

	public static FlightSearch of(String departureAirportCode, String arrivalAirportCode,
		LocalDate outboundDate, LocalDate returnDate, String departureToken) {
		FlightSearch flightSearch = new FlightSearch();
		flightSearch.searchedAt = LocalDateTime.now();
		flightSearch.departureAirportCode = departureAirportCode;
		flightSearch.arrivalAirportCode = arrivalAirportCode;
		flightSearch.outboundDate = outboundDate;
		flightSearch.returnDate = returnDate;
		flightSearch.departureToken = departureToken;
		return flightSearch;
	}
}