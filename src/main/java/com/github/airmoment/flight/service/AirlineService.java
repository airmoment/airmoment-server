package com.github.airmoment.flight.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.airmoment.flight.domain.Airline;
import com.github.airmoment.flight.repository.AirlineRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AirlineService {

	private final AirlineRepository airlineRepository;

	@Transactional
	public String findOrCreatePhoto(String airlineName) {
		if (airlineName == null || airlineName.isBlank()) {
			return null;
		}
		return airlineRepository.findByName(airlineName)
			.orElseGet(() -> airlineRepository.save(Airline.of(airlineName)))
			.getPhoto();
	}
}
