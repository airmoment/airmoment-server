package com.github.airmoment.flight.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.airmoment.flight.domain.FlightOffer;
import com.github.airmoment.flight.domain.enums.FlightDirection;

@Repository
public interface FlightOfferRepository extends JpaRepository<FlightOffer, Long> {

	@Query("SELECT MIN(fo.price) FROM FlightOffer fo "
		+ "WHERE fo.flightSearch.departureAirportCode = :dep "
		+ "AND fo.flightSearch.arrivalAirportCode = :arr "
		+ "AND fo.flightSearch.outboundDate = :date "
		+ "AND fo.direction = :direction "
		+ "AND fo.flightSearch.searchedAt <= :now "
		+ "GROUP BY fo.flightSearch.id "
		+ "ORDER BY MAX(fo.flightSearch.searchedAt) DESC")
	List<Integer> findRecentMinOutboundPrices(
		@Param("dep") String dep,
		@Param("arr") String arr,
		@Param("date") LocalDate date,
		@Param("direction") FlightDirection direction,
		@Param("now") LocalDateTime now,
		Pageable pageable
	);
}
