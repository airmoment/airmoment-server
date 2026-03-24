package com.github.airmoment.flight.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.stereotype.Service;

import com.github.airmoment.flight.domain.FlightLayover;
import com.github.airmoment.flight.domain.FlightOffer;
import com.github.airmoment.flight.domain.FlightPriceHistory;
import com.github.airmoment.flight.domain.FlightPriceInsight;
import com.github.airmoment.flight.domain.FlightSearch;
import com.github.airmoment.flight.domain.FlightSegment;
import com.github.airmoment.flight.domain.enums.FlightDirection;
import com.github.airmoment.flight.repository.FlightLayoverRepository;
import com.github.airmoment.flight.repository.FlightOfferRepository;
import com.github.airmoment.flight.repository.FlightPriceHistoryRepository;
import com.github.airmoment.flight.repository.FlightPriceInsightRepository;
import com.github.airmoment.flight.repository.FlightSearchRepository;
import com.github.airmoment.flight.repository.FlightSegmentRepository;
import com.github.airmoment.global.client.serpapi.dto.FlightOfferDto;
import com.github.airmoment.global.client.serpapi.dto.FlightSearchResponse;
import com.github.airmoment.global.client.serpapi.dto.FlightSegmentDto;
import com.github.airmoment.global.client.serpapi.dto.LayoverDto;
import com.github.airmoment.global.client.serpapi.dto.PriceInsightDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightDataService {

	private final FlightSearchRepository flightSearchRepository;
	private final FlightOfferRepository flightOfferRepository;
	private final FlightLayoverRepository flightLayoverRepository;
	private final FlightPriceHistoryRepository flightPriceHistoryRepository;
	private final FlightPriceInsightRepository flightPriceInsightRepository;
	private final FlightSegmentRepository flightSegmentRepository;

	public FlightSearch saveFlightSearch(String departureAirportCode, String arrivalAirportCode, LocalDate departureDate) {
		FlightSearch flightSearch = FlightSearch.of(
			departureAirportCode,
			arrivalAirportCode,
			departureDate
		);

		return flightSearchRepository.save(flightSearch);
	}

	public void saveFlights(FlightSearch flightSearch, FlightSearchResponse response, FlightDirection direction) {
		// FlightOffer, FlightSegment, FlightLayover 저장
		saveFlightOffers(flightSearch, response.bestFlights(), true, direction);
		saveFlightOffers(flightSearch, response.otherFlights(), false, direction);

		// PriceInsight 저장
		if (response.priceInsights() != null) {
			savePriceInsight(flightSearch, response.priceInsights());
		}
	}

	private void saveFlightOffers(FlightSearch flightSearch,
		List<FlightOfferDto> offerDtos, boolean isBest, FlightDirection direction) {
		if (offerDtos == null || offerDtos.isEmpty()) return;

		for (FlightOfferDto offerDto : offerDtos) {

			// price가 null이면 저장 의미 없으므로 skip
			if (offerDto.price() == null) {
				log.warn("price가 null인 항공편 skip - flightNumber: {}",
					offerDto.flights() != null && !offerDto.flights().isEmpty()
						? offerDto.flights().get(0).flightNumber()
						: "unknown");
				continue;
			}

			// FlightOffer 저장
			boolean hasLayover = offerDto.layovers() != null && !offerDto.layovers().isEmpty();
			int layoverCount = hasLayover ? offerDto.layovers().size() : 0;

			FlightOffer flightOffer = FlightOffer.of(
				flightSearch,
				offerDto.price(),
				offerDto.totalDuration(),
				hasLayover,
				layoverCount,
				isBest,
				direction
			);
			flightOfferRepository.save(flightOffer);

			// FlightSegment 저장
			saveFlightSegments(flightOffer, offerDto.flights());

			// FlightLayover 저장
			if (hasLayover) {
				saveFlightLayovers(flightOffer, offerDto.layovers());
			}
		}
	}

	private void saveFlightSegments(FlightOffer flightOffer, List<FlightSegmentDto> segmentDtos) {
		if (segmentDtos == null || segmentDtos.isEmpty()) return;

		for (int i = 0; i < segmentDtos.size(); i++) {
			FlightSegmentDto dto = segmentDtos.get(i);

			FlightSegment segment = FlightSegment.of(
				flightOffer,
				i + 1,  // segment_order (1부터 시작)
				dto.departureAirport().id(),
				dto.departureAirport().name(),
				parseDateTime(dto.departureAirport().time()),
				dto.arrivalAirport().id(),
				dto.arrivalAirport().name(),
				parseDateTime(dto.arrivalAirport().time()),
				dto.duration(),
				dto.airline(),
				dto.flightNumber(),
				dto.travelClass(),
				dto.legroom(),
				dto.airplane()
			);
			flightSegmentRepository.save(segment);
		}
	}

	private void saveFlightLayovers(FlightOffer flightOffer, List<LayoverDto> layoverDtos) {
		for (int i = 0; i < layoverDtos.size(); i++) {
			LayoverDto dto = layoverDtos.get(i);

			FlightLayover layover = FlightLayover.of(
				flightOffer,
				i + 1,  // layover_order (1부터 시작)
				dto.id(),
				dto.name(),
				dto.duration(),
				dto.overnight() != null && dto.overnight()
			);
			flightLayoverRepository.save(layover);
		}
	}

	private void savePriceInsight(FlightSearch flightSearch, PriceInsightDto dto) {
		// typical_price_range는 [min, max] 배열
		Integer typicalPriceMin = null;
		Integer typicalPriceMax = null;
		if (dto.typicalPriceRange() != null && dto.typicalPriceRange().size() == 2) {
			typicalPriceMin = dto.typicalPriceRange().get(0);
			typicalPriceMax = dto.typicalPriceRange().get(1);
		}

		FlightPriceInsight insight = FlightPriceInsight.of(
			flightSearch,
			dto.lowestPrice(),
			dto.priceLevel(),
			typicalPriceMin,
			typicalPriceMax
		);
		flightPriceInsightRepository.save(insight);

		// price_history 저장
		if (dto.priceHistory() != null) {
			savePriceHistories(flightSearch, dto.priceHistory());
		}
	}

	private void savePriceHistories(FlightSearch flightSearch, List<List<Long>> priceHistory) {
		for (List<Long> entry : priceHistory) {
			if (entry == null || entry.size() < 2) continue;

			FlightPriceHistory history = FlightPriceHistory.of(
				flightSearch,
				entry.get(0),           // timestamp
				entry.get(1).intValue() // price
			);
			flightPriceHistoryRepository.save(history);
		}
	}

	private LocalDateTime parseDateTime(String dateTimeStr) {
		if (dateTimeStr == null || dateTimeStr.isBlank()) return null;
		return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
	}
}