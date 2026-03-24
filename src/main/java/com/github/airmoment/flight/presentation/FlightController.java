package com.github.airmoment.flight.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.airmoment.flight.scheduler.FlightDataScheduler;
import com.github.airmoment.flight.scheduler.FlightReportScheduler;
import com.github.airmoment.global.response.dto.SuccessResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/flights")
@RequiredArgsConstructor
public class FlightController {

	private final FlightDataScheduler flightDataScheduler;
	private final FlightReportScheduler flightReportScheduler;

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
