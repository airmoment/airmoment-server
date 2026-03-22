package com.github.airmoment.flight.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.airmoment.flight.domain.FlightOffer;

@Repository
public interface FlightOfferRepository extends JpaRepository<FlightOffer, Long> {
}
