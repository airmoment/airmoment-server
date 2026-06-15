package com.github.airmoment.flight.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.airmoment.flight.domain.enums.FlightDirection;
import com.github.airmoment.flight.dto.CachedFlightResult;
import com.github.airmoment.flight.dto.DailyOilPrice;
import com.github.airmoment.flight.dto.FlightFeatureVector;
import com.github.airmoment.flight.dto.ForecastDto;
import com.github.airmoment.flight.dto.ForecastRequest;
import com.github.airmoment.flight.dto.GraphResponse;
import com.github.airmoment.flight.repository.FlightOfferRepository;
import com.github.airmoment.flight.repository.HolidayRepository;
import com.github.airmoment.flight.scheduler.OilPriceScheduler;
import com.github.airmoment.global.client.fastapi.AIServerClient;
import com.github.airmoment.global.client.frankfurter.ExchangeRateClient;
import com.github.airmoment.global.client.frankfurter.dto.FrankfurterResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightForecastService {

	private static final ZoneId SYSTEM_ZONE = ZoneId.systemDefault();
	private static final String REDIS_KEY_OIL_LATEST = "oil:price:latest";
	private static final String REDIS_KEY_OIL_7DAYS_AGO = "oil:price:7days-ago";
	private static final String CURRENCY_KRW = "KRW";
	private static final int FX_STALE_DAYS = 5;

	// 도착 공항코드 → 통화 매핑
	private static final Map<String, String> ARRIVAL_CURRENCY = Map.ofEntries(
		Map.entry("CDG", "EUR"),
		Map.entry("JFK", "USD"),
		Map.entry("SYD", "AUD")
	);

	private final FlightFeatureService flightFeatureService;
	private final FlightOfferRepository flightOfferRepository;
	private final HolidayRepository holidayRepository;
	private final OilPriceScheduler oilPriceScheduler;
	private final ExchangeRateClient exchangeRateClient;
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;
	private final AIServerClient aiServerClient;

	public GraphResponse forecast(String departureCode, String arrivalCode, LocalDate departureAt, CachedFlightResult cached) {
		ForecastRequest request = buildForecastRequest(departureCode, arrivalCode, departureAt, cached);
		log.info("ForecastRequest 생성 완료 - route: {}, departureAt: {}", request.routeId(), departureAt);
		ForecastDto dto = aiServerClient.forecast(request);
		return GraphResponse.from(request.routeId(), request.daysToDeparture(), dto);
	}

	/**
	 * 가격 예측/설명 요청에 공통으로 쓰이는 ForecastRequest 를 생성한다.
	 */
	public ForecastRequest buildForecastRequest(String departureCode, String arrivalCode, LocalDate departureAt, CachedFlightResult cached) {
		LocalDateTime now = LocalDateTime.now(SYSTEM_ZONE);
		FlightFeatureVector fv = flightFeatureService.calculate(departureCode, arrivalCode, departureAt, cached);

		// outboundMonth, outboundDayOfWeek
		int outboundMonth = departureAt.getMonthValue();
		String outboundDayOfWeek = departureAt.getDayOfWeek().name().substring(0, 3);

		// isPeakSeason: 7~8월, 12~1월
		int isPeakSeason = (outboundMonth >= 7 && outboundMonth <= 8)
			|| outboundMonth == 12 || outboundMonth == 1 ? 1 : 0;

		// isHolidayNear: 출발일 기준 ±3일 내 공휴일
		int isHolidayNear = holidayRepository.existsByDateBetween(
			departureAt.minusDays(3), departureAt.plusDays(3)) ? 1 : 0;

		// lag1Price: 1일 전 동일 노선 최저가
		List<Integer> lag1Obs = flightOfferRepository.findRecentMinOutboundPrices(
			departureCode, arrivalCode, departureAt, FlightDirection.OUTBOUND, now, PageRequest.of(0, 1));
		Integer lag1Price = lag1Obs.isEmpty() ? null : lag1Obs.get(0);

		// priceChange1: 현재가 - lag1 (int)
		Integer priceChange1 = lag1Price != null ? fv.currentCheapestPrice() - lag1Price : null;

		// priceVsRollingMean3: int 변환
		Integer priceVsRollingMean3 = fv.priceVsRollingMean3() != null
			? Math.round(fv.priceVsRollingMean3()) : null;

		// priceLevel: typicalPriceMin/Max 기준 low/typical/high
		String priceLevel = computePriceLevel(fv.currentCheapestPrice(), cached.typicalPriceMin(), cached.typicalPriceMax());

		// 유가 피처
		Float oilPriceUsd = readOilPrice(REDIS_KEY_OIL_LATEST);
		Float oilChange7d = computeOilChange7d(oilPriceUsd);

		// 환율 변화율 피처
		Float arrFxChange7d = computeArrFxChange7d(arrivalCode, departureAt);

		ForecastRequest request = new ForecastRequest(
			fv.routeId(),
			fv.currentCheapestPrice(),
			fv.daysToDeparture(),
			outboundMonth,
			fv.searchedDayOfWeek(),
			outboundDayOfWeek,
			fv.isWeekendSearch() ? 1 : 0,
			isPeakSeason,
			isHolidayNear,
			fv.isLongHaul() ? 1 : 0,
			fv.offerCount(),
			fv.nonstopRatio(),
			fv.cheapestNonstopPrice(),
			fv.cheapestOfferHasLayover() ? 1 : 0,
			priceLevel,
			fv.currGapToTypicalMin(),
			fv.currGapToTypicalMax(),
			fv.histRecentStd(),
			fv.histRecentSlope(),
			fv.currVsHistMean(),
			lag1Price,
			priceChange1,
			fv.rollingStd3(),
			priceVsRollingMean3,
			oilPriceUsd,
			oilChange7d,
			arrFxChange7d
		);

		return request;
	}

	private String computePriceLevel(int currentPrice, Integer typicalMin, Integer typicalMax) {
		if (typicalMin == null || typicalMax == null) return null;
		if (currentPrice < typicalMin) return "low";
		if (currentPrice > typicalMax) return "high";
		return "typical";
	}

	private Float readOilPrice(String redisKey) {
		try {
			String json = redisTemplate.opsForValue().get(redisKey);
			if (json == null) {
				log.info("유가 Redis 데이터 없음, 스케줄러 수동 호출 - key: {}", redisKey);
				oilPriceScheduler.updateOilPrice();
				json = redisTemplate.opsForValue().get(redisKey);
			}
			if (json == null) return null;
			DailyOilPrice oil = objectMapper.readValue(json, DailyOilPrice.class);
			return (float) oil.price();
		} catch (Exception e) {
			log.warn("유가 Redis 조회 실패 - key: {}, error: {}", redisKey, e.getMessage());
			return null;
		}
	}

	private Float computeOilChange7d(Float latestOilPrice) {
		Float sevenDaysAgoPrice = readOilPrice(REDIS_KEY_OIL_7DAYS_AGO);
		if (latestOilPrice == null || sevenDaysAgoPrice == null) return null;
		return latestOilPrice - sevenDaysAgoPrice;
	}

	private Float computeArrFxChange7d(String arrivalCode, LocalDate today) {
		String currency = ARRIVAL_CURRENCY.get(arrivalCode);
		if (currency == null) return null;

		try {
			FrankfurterResponse current = exchangeRateClient.fetchLatest(currency);
			FrankfurterResponse sevenDaysAgo = exchangeRateClient.fetchByDate(today.minusDays(7), currency);

			if (current == null || sevenDaysAgo == null) return null;

			// 데이터가 5일 이상 오래된 경우 null
			if (current.date() != null) {
				LocalDate dataDate = LocalDate.parse(current.date());
				if (ChronoUnit.DAYS.between(dataDate, today) >= FX_STALE_DAYS) return null;
			}

			Double currentRate = current.rate(CURRENCY_KRW);
			Double pastRate = sevenDaysAgo.rate(CURRENCY_KRW);

			if (currentRate == null || pastRate == null || pastRate == 0) return null;
			return (float) ((currentRate - pastRate) / pastRate);

		} catch (Exception e) {
			log.warn("환율 변화율 계산 실패 - arrival: {}, error: {}", arrivalCode, e.getMessage());
			return null;
		}
	}
}
