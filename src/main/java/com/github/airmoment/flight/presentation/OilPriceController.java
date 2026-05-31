package com.github.airmoment.flight.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.airmoment.flight.scheduler.OilPriceScheduler;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/oil-price")
@RequiredArgsConstructor
public class OilPriceController {

	private final OilPriceScheduler oilPriceScheduler;

	@PostMapping("/refresh")
	public ResponseEntity<Void> refresh() {
		oilPriceScheduler.updateOilPrice();
		return ResponseEntity.ok().build();
	}
}
