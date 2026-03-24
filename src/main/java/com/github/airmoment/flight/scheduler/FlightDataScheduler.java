package com.github.airmoment.flight.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.airmoment.flight.domain.FlightSearch;
import com.github.airmoment.flight.domain.enums.FlightDirection;
import com.github.airmoment.flight.service.FlightDataService;
import com.github.airmoment.global.client.serpapi.SerpApiClient;
import com.github.airmoment.global.client.serpapi.dto.FlightSearchResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlightDataScheduler {

	private final SerpApiClient serpApiClient;
	private final FlightDataService flightDataService;

	private static final String ORIGIN = "ICN";
	private static final List<String> TARGET_AIRPORTS = List.of("NRT", "DAD", "CDG");

	@Scheduled(cron = "0 0 0,12 * * *", zone = "Asia/Seoul")  // 매일 00시, 12시
	public void collectFlightData() {
		log.info("항공편 데이터 수집 시작: {}", LocalDateTime.now());

		try {
			LocalDate dynamicOutboundDate = LocalDate.now().plusMonths(1);
			LocalDate dynamicInboundDate = dynamicOutboundDate.plusDays(6);
			collectForPeriod(dynamicOutboundDate, dynamicInboundDate);

			collectForPeriod(LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 19));

			log.info("항공편 데이터 수집 완료");
		} catch (Exception e) {
			log.error("항공편 데이터 수집 실패: {}", e.getMessage());
		}
	}

	private void collectForPeriod(LocalDate dynamicOutboundDate, LocalDate dynamicInboundDate) {
		for (String targetAirport : TARGET_AIRPORTS) {
			FlightSearch outboundFlightSearch = flightDataService.saveFlightSearch(ORIGIN, targetAirport,
				dynamicOutboundDate);
			FlightSearchResponse outboundResponse = serpApiClient.fetchFlights(
				ORIGIN, targetAirport, dynamicOutboundDate.toString()
			);
			log.info("outboundResponse: {}", outboundResponse);
			flightDataService.saveFlights(outboundFlightSearch, outboundResponse, FlightDirection.OUTBOUND);

			FlightSearch inboundFlightSearch = flightDataService.saveFlightSearch(targetAirport, ORIGIN,
				dynamicInboundDate);
			FlightSearchResponse inboundResponse = serpApiClient.fetchFlights(
				targetAirport, ORIGIN, dynamicInboundDate.toString()
			);
			log.info("inboundResponse: {}", inboundResponse);
			flightDataService.saveFlights(inboundFlightSearch, inboundResponse, FlightDirection.INBOUND);
		}
	}
}