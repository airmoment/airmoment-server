package com.github.airmoment.flight.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.airmoment.flight.domain.Airline;

public interface AirlineRepository extends JpaRepository<Airline, Long> {
	Optional<Airline> findByName(String name);
}
