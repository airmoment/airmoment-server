package com.github.airmoment.flight.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
	private static final List<String> TARGET_AIRPORTS = List.of("CDG", "JFK", "SYD");
	private static final List<LocalDate> FIXED_OUTBOUND_DATES = List.of(
		LocalDate.of(2026, 7, 25),
		LocalDate.of(2026, 11, 15),
		LocalDate.of(2026, 12, 25)
	);

	private static final ZoneId SCHEDULER_ZONE = ZoneId.of("Asia/Seoul");


	@Scheduled(cron = "0 0 0,12 * * *", zone = "Asia/Seoul")  // 매일 00시, 12시
	public void collectFlightData() {

		// 동적으로 변하는 출발일자 : 현재로부터 한 달 후
		LocalDate dynamicOutboundDate = LocalDate.now(SCHEDULER_ZONE).plusMonths(1);
		log.info("항공편 데이터 수집 시작: {}", LocalDateTime.now(SCHEDULER_ZONE));

		try {
			collectForPeriod(dynamicOutboundDate);
			log.info("항공편 데이터 수집 완료");
		} catch (Exception e) {
			log.error("항공편 데이터 수집 실패: {}", e.getMessage());
		}
	}

	private void collectForPeriod(LocalDate dynamicOutboundDate) {
		for (String targetAirport : TARGET_AIRPORTS) {
			fetchAndSaveData(targetAirport, dynamicOutboundDate);
			for (LocalDate outboundDate : FIXED_OUTBOUND_DATES) {
				fetchAndSaveData(targetAirport, outboundDate);
			}
		}
	}

	private void fetchAndSaveData(String targetAirport, LocalDate outboundDate) {
		try{
			FlightSearch outboundFlightSearch = flightDataService.saveFlightSearch(ORIGIN, targetAirport, outboundDate);
			FlightSearchResponse outboundResponse = serpApiClient.fetchFlights(ORIGIN, targetAirport, outboundDate.toString());
			flightDataService.saveFlights(outboundFlightSearch, outboundResponse, FlightDirection.OUTBOUND);
			}
			catch(Exception e) {
			log.error("{}에 출발하는 {}행 항공편 데이터 수집이 실패하였습니다. ", outboundDate, targetAirport);
			}
	}
}