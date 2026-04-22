package com.github.airmoment.flight.presentation;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.airmoment.flight.domain.enums.FlightSortOption;
import com.github.airmoment.flight.dto.FlightListResponse;
import com.github.airmoment.flight.scheduler.FlightDataScheduler;
import com.github.airmoment.flight.scheduler.FlightReportScheduler;
import com.github.airmoment.flight.service.FlightSearchService;
import com.github.airmoment.global.response.dto.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightController {

	private final FlightDataScheduler flightDataScheduler;
	private final FlightReportScheduler flightReportScheduler;
	private final FlightSearchService flightSearchService;

	@GetMapping
	public ResponseEntity<SuccessResponse<FlightListResponse>> searchFlights(
		@RequestParam String departureCode,
		@RequestParam String arrivalCode,
		@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate departureAt,
		@RequestParam(required = false) FlightSortOption sort,
		@RequestParam(required = false) Boolean nonstopOnly,
		@RequestParam(required = false) Integer maxPrice
	) {
		FlightListResponse response = flightSearchService.searchFlights(
			departureCode, arrivalCode, departureAt, sort, nonstopOnly, maxPrice);
		return ResponseEntity.ok(new SuccessResponse<>(200, "항공권 조회 성공", response));
	}

	@PostMapping("/dataScheduler")
	public ResponseEntity<SuccessResponse<Void>> runDataScheduler() {
		flightDataScheduler.collectFlightData();
		return ResponseEntity.ok()
			.body(SuccessResponse.of(200, "항공권 데이터가 조회되어 db에 저장되었습니다."));
	}

	@PostMapping("/reportScheduler")
	public ResponseEntity<SuccessResponse<Void>> runReportScheduler() {
		flightReportScheduler.reportDailyData();
		return ResponseEntity.ok()
			.body(SuccessResponse.of(200, "항공권 데이터가 시트에 업데이트되었습니다."));
	}
}
