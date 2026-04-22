package com.github.airmoment.flight.service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.airmoment.exception.FlightErrorCode;
import com.github.airmoment.flight.domain.Airline;
import com.github.airmoment.flight.domain.enums.FlightSortOption;
import com.github.airmoment.flight.dto.AIPredictionResponse;
import com.github.airmoment.flight.dto.CachedFlightItem;
import com.github.airmoment.flight.dto.CachedFlightResult;
import com.github.airmoment.flight.dto.FlightFeatureVector;
import com.github.airmoment.flight.dto.FlightItemResponse;
import com.github.airmoment.flight.dto.FlightListResponse;
import com.github.airmoment.flight.dto.FlightPredictDto;
import com.github.airmoment.flight.repository.AirlineRepository;
import com.github.airmoment.global.client.fastapi.AIServerClient;
import com.github.airmoment.global.client.serpapi.SerpApiClient;
import com.github.airmoment.global.client.serpapi.dto.FlightOfferDto;
import com.github.airmoment.global.client.serpapi.dto.FlightSearchResponse;
import com.github.airmoment.global.client.serpapi.dto.FlightSegmentDto;
import com.github.airmoment.global.exception.AirmomentException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlightSearchService {

	private static final String CACHE_KEY_FORMAT = "flights:%s:%s:%s";
	private static final Duration CACHE_TTL = Duration.ofHours(12);
	private static final DateTimeFormatter SEGMENT_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

	private final SerpApiClient serpApiClient;
	private final AirlineRepository airlineRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;
	private final FlightFeatureService flightFeatureService;
	private final AIServerClient aiServerClient;

	@Transactional
	public FlightListResponse searchFlights(
		String departureCode,
		String arrivalCode,
		LocalDate departureAt,
		FlightSortOption sort,
		Boolean nonstopOnly,
		Integer maxPrice
	) {
		String cacheKey = String.format(CACHE_KEY_FORMAT, departureCode, arrivalCode, departureAt);

		CachedFlightResult cached = getFromCache(cacheKey);
		if (cached == null) {
			FlightSearchResponse response = serpApiClient.fetchFlights(departureCode, arrivalCode, departureAt.toString());
			cached = buildCachedResult(response);
			saveToCache(cacheKey, cached);
		}

		FlightSortOption effectiveSort = sort != null ? sort : FlightSortOption.DEPARTURE_TIME_ASC;

		List<FlightItemResponse> result = cached.flights().stream()
			.filter(item -> nonstopOnly == null || !nonstopOnly || item.nonstop())
			.filter(item -> maxPrice == null || item.price() <= maxPrice)
			.sorted(getComparator(effectiveSort))
			.map(FlightItemResponse::from)
			.toList();

		// 입력값, 항공권 조회값을 바탕으로 featureVector 계산
		FlightFeatureVector featureVector = flightFeatureService.calculate(departureCode, arrivalCode, departureAt, cached);
		log.info("입력한 조건의 항공권 결과에 대한 feature vector 계산이 완료되었습니다. \n****FeatureVector****\n {}", featureVector);

		AIPredictionResponse prediction = null;
		try {
			prediction = getPrediction(featureVector);
		} catch (Exception e) {
			log.error("AI 서버의 예측 API를 호출이 실패하였습니다.\nerror:{}", e.getMessage());
		}

		if (prediction != null) {
			FlightPredictDto predictDto = new FlightPredictDto(prediction.decision());
			return FlightListResponse.of(predictDto, result);
		}
		else {
			throw new AirmomentException(FlightErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private AIPredictionResponse getPrediction(FlightFeatureVector featureVector) {
		return aiServerClient.predict(featureVector);
	}

	private CachedFlightResult buildCachedResult(FlightSearchResponse response) {
		List<FlightOfferDto> allOffers = new ArrayList<>();
		if (response.bestFlights() != null) allOffers.addAll(response.bestFlights());
		if (response.otherFlights() != null) allOffers.addAll(response.otherFlights());

		List<CachedFlightItem> flights = allOffers.stream()
			.filter(offer -> offer.price() != null
				&& offer.flights() != null
				&& !offer.flights().isEmpty())
			.map(this::toCachedItem)
			.toList();

		Integer typicalPriceMin = null;
		Integer typicalPriceMax = null;
		if (response.priceInsights() != null && response.priceInsights().typicalPriceRange() != null
			&& response.priceInsights().typicalPriceRange().size() >= 2) {
			typicalPriceMin = response.priceInsights().typicalPriceRange().get(0);
			typicalPriceMax = response.priceInsights().typicalPriceRange().get(1);
		}

		return new CachedFlightResult(flights, typicalPriceMin, typicalPriceMax);
	}

	private CachedFlightItem toCachedItem(FlightOfferDto offer) {
		FlightSegmentDto firstSegment = offer.flights().get(0);
		FlightSegmentDto lastSegment = offer.flights().get(offer.flights().size() - 1);

		String airlineName = firstSegment.airline();
		String airlinePhoto = resolveAirlinePhoto(airlineName);

		LocalTime departureTime = parseTime(firstSegment.departureAirport().time());
		LocalTime arrivalTime = parseTime(lastSegment.arrivalAirport().time());
		boolean nonstop = offer.layovers() == null || offer.layovers().isEmpty();

		return new CachedFlightItem(airlineName, airlinePhoto, departureTime, arrivalTime,
			offer.totalDuration(), offer.price(), nonstop);
	}

	private String resolveAirlinePhoto(String airlineName) {
		if (airlineName == null || airlineName.isBlank()) {
			return null;
		}
		return airlineRepository.findByName(airlineName)
			.orElseGet(() -> airlineRepository.save(Airline.of(airlineName)))
			.getPhoto();
	}

	private LocalTime parseTime(String timeStr) {
		if (timeStr == null || timeStr.isBlank()) return null;
		return LocalDateTime.parse(timeStr, SEGMENT_TIME_FORMATTER).toLocalTime();
	}

	private Comparator<CachedFlightItem> getComparator(FlightSortOption sort) {
		return switch (sort) {
			case DEPARTURE_TIME_ASC ->
				Comparator.comparing(CachedFlightItem::departureTime, Comparator.nullsLast(Comparator.naturalOrder()));
			case DEPARTURE_TIME_DESC ->
				Comparator.comparing(CachedFlightItem::departureTime, Comparator.nullsLast(Comparator.reverseOrder()));
			case PRICE_ASC ->
				Comparator.comparingInt(CachedFlightItem::price);
		};
	}

	private CachedFlightResult getFromCache(String key) {
		String json = redisTemplate.opsForValue().get(key);
		if (json == null) return null;
		try {
			return objectMapper.readValue(json, CachedFlightResult.class);
		} catch (JsonProcessingException e) {
			log.warn("Redis 캐시 역직렬화 실패 - key: {}, error: {}", key, e.getMessage());
			return null;
		}
	}

	private void saveToCache(String key, CachedFlightResult result) {
		try {
			redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(result), CACHE_TTL);
		} catch (JsonProcessingException e) {
			log.warn("Redis 캐시 저장 실패 - key: {}, error: {}", key, e.getMessage());
		}
	}
}
