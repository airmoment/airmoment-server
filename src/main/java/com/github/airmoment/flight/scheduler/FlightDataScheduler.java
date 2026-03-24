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
			FlightSearch outboundFlightSearch = flightDataService.saveFlightSearch(ORIGIN, targetAirport, departureDate,
				returnDate, null);
			FlightSearchResponse outboundResponse = serpApiClient.fetchOutBoundFlights(
				ORIGIN, targetAirport,
				departureDate.toString(),
				returnDate.toString()
			);
			log.info("outboundResponse: {}", outboundResponse);
			flightDataService.saveFlights(outboundFlightSearch, outboundResponse, FlightDirection.OUTBOUND);

			List<String> departureTokens = extractTop3DepartureTokens(outboundResponse);
			for (String departureToken : departureTokens) {
				FlightSearch inboundFlightSearch = flightDataService.saveFlightSearch(ORIGIN, targetAirport,
					departureDate, returnDate, departureToken);
				FlightSearchResponse inboundResponse = serpApiClient.fetchInBoundFlights(
					departureToken,
					ORIGIN, targetAirport,
					departureDate.toString(),
					returnDate.toString()
				);
				log.info("inResponse: {}", inboundResponse);
				flightDataService.saveFlights(inboundFlightSearch, inboundResponse);
			}
		}
	}

	private List<String> extractTop3DepartureTokens(FlightSearchResponse response) {
		List<FlightOfferDto> flights =
			response.bestFlights() == null ? response.otherFlights() : response.bestFlights();

		return flights
			.stream()
			.limit(3)
			.map(FlightOfferDto::departureToken)
			.filter(token -> token != null && !token.isBlank())
			.toList();
	}
}