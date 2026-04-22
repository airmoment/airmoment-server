package com.github.airmoment.flight.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.airmoment.flight.domain.FlightPriceHistory;

@Repository
public interface FlightPriceHistoryRepository extends JpaRepository<FlightPriceHistory, Long> {

	@Query("SELECT fph.price FROM FlightPriceHistory fph "
		+ "WHERE fph.flightSearch.departureAirportCode = :dep "
		+ "AND fph.flightSearch.arrivalAirportCode = :arr "
		+ "AND fph.flightSearch.outboundDate = :date "
		+ "AND fph.flightSearch.searchedAt <= :now "
		+ "ORDER BY fph.timeStamp DESC")
	List<Integer> findLatestHistoryPrices(
		@Param("dep") String dep,
		@Param("arr") String arr,
		@Param("date") LocalDate date,
		@Param("now") LocalDateTime now,
		Pageable pageable
	);
}
