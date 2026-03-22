package com.github.airmoment.flight.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.airmoment.flight.scheduler.FlightDataScheduler;
import com.github.airmoment.global.response.dto.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightController {

	private final FlightDataScheduler flightDataScheduler;

	@PostMapping("/runScheduler")
	public ResponseEntity<SuccessResponse<Void>> runScheduler(){
		flightDataScheduler.collectFlightData();
		return ResponseEntity.ok()
			.body(SuccessResponse.of(200, "항공권 데이터가 조회 및 저장되었습니다."));
	}
}
