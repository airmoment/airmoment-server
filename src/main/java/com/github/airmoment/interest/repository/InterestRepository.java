package com.github.airmoment.interest.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.airmoment.flight.domain.enums.AirportCode;
import com.github.airmoment.interest.domain.Interest;

public interface InterestRepository extends JpaRepository<Interest, Long> {

	List<Interest> findAllByMemberIdAndIsBookmarkedTrueOrderByCreatedAtDesc(Long memberId);

	Optional<Interest> findByMemberIdAndDepartureCodeAndArrivalCodeAndDepartureAtAndNonstopOnly(
		Long memberId, AirportCode departureCode, AirportCode arrivalCode,
		LocalDate departureAt, boolean nonstopOnly);
}
