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
@Table(name = "flight_price_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlightPriceHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long timeStamp;

	@Column(nullable = false)
	private Integer price;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flight_search_id", nullable = false)
	private FlightSearch flightSearch;

	public static FlightPriceHistory of(FlightSearch flightSearch, Long timeStamp, Integer price) {
		FlightPriceHistory history = new FlightPriceHistory();
		history.flightSearch = flightSearch;
		history.timeStamp = timeStamp;
		history.price = price;
		return history;
	}
}
