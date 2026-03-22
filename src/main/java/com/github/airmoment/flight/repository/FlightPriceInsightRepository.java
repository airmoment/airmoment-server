package com.github.airmoment.flight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.airmoment.flight.domain.FlightPriceInsight;

@Repository
public interface FlightPriceInsightRepository extends JpaRepository<FlightPriceInsight, Long> {
}
