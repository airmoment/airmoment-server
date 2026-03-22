package com.github.airmoment.flight.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flight_price_insight")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FlightPriceInsight {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Integer lowestPrice;

	@Column(nullable = false, length = 20)
	private String priceLevel;

	@Column(nullable = false)
	private Integer typicalPriceMin;

	@Column(nullable = false)
	private Integer typicalPriceMax;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "flight_search_id", nullable = false)
	private FlightSearch flightSearch;

	public static FlightPriceInsight of(FlightSearch flightSearch, Integer lowestPrice,
		String priceLevel, Integer typicalPriceMin,
		Integer typicalPriceMax) {
		FlightPriceInsight insight = new FlightPriceInsight();
		insight.flightSearch = flightSearch;
		insight.lowestPrice = lowestPrice;
		insight.priceLevel = priceLevel;
		insight.typicalPriceMin = typicalPriceMin;
		insight.typicalPriceMax = typicalPriceMax;
		return insight;
	}
}
