package com.github.airmoment.flight.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.airmoment.flight.domain.FlightPriceInsight;

@Repository
public interface FlightPriceInsightRepository extends JpaRepository<FlightPriceInsight, Long> {

	@Query("SELECT fpi.lowestPrice FROM FlightPriceInsight fpi "
		+ "WHERE fpi.flightSearch.departureAirportCode = :dep "
		+ "AND fpi.flightSearch.arrivalAirportCode = :arr "
		+ "AND fpi.flightSearch.outboundDate = :date "
		+ "AND fpi.lowestPrice IS NOT NULL "
		+ "ORDER BY fpi.flightSearch.searchedAt DESC")
	List<Integer> findRecentLowestPrices(
		@Param("dep") String dep,
		@Param("arr") String arr,
		@Param("date") LocalDate date,
		Pageable pageable
	);
}
