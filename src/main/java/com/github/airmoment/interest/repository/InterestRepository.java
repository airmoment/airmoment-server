package com.github.airmoment.interest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.airmoment.interest.domain.Interest;

public interface InterestRepository extends JpaRepository<Interest, Long> {
}
