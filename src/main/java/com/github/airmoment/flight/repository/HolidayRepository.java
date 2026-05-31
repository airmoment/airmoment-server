package com.github.airmoment.flight.repository;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.airmoment.flight.domain.Holiday;

@Repository
public interface HolidayRepository extends JpaRepository<Holiday, LocalDate> {

	boolean existsByDateBetween(LocalDate from, LocalDate to);
}
