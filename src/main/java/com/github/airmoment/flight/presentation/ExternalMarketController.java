package com.github.airmoment.flight.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.airmoment.flight.scheduler.ExchangeRateScheduler;
import com.github.airmoment.flight.scheduler.OilPriceScheduler;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ExternalMarketController {

	private final OilPriceScheduler oilPriceScheduler;
	private final ExchangeRateScheduler exchangeRateScheduler;

	@PostMapping("/oil-price/refresh")
	public ResponseEntity<Void> refreshOilPrice() {
		oilPriceScheduler.updateOilPrice();
		return ResponseEntity.ok().build();
	}

	@PostMapping("/exchange-rate/refresh")
	public ResponseEntity<Void> refreshExchangeRate() {
		exchangeRateScheduler.updateExchangeRate();
		return ResponseEntity.ok().build();
	}
}
