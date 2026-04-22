package com.github.airmoment.flight.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.airmoment.flight.domain.enums.FlightDirection;
import com.github.airmoment.flight.dto.CachedFlightItem;
import com.github.airmoment.flight.dto.CachedFlightResult;
import com.github.airmoment.flight.dto.FlightFeatureVector;
import com.github.airmoment.flight.repository.FlightOfferRepository;
import com.github.airmoment.flight.repository.FlightPriceHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FlightFeatureService {

	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private static final int ROLLING_WINDOW = 3;
	private static final int HISTORY_WINDOW = 5;

	// 장거리 노선 사전 정의 (편도 기준 6시간 이상)
	private static final Set<String> LONG_HAUL_ROUTES = Set.of(
		"ICN-CDG", "ICN-JFK", "ICN-SYD", "ICN-LAX", "ICN-LHR", "ICN-ORD",
		"ICN-YVR", "ICN-SFO", "ICN-SEA", "ICN-ATL", "ICN-DFW", "ICN-YYZ",
		"CDG-ICN", "JFK-ICN", "SYD-ICN", "LAX-ICN", "LHR-ICN", "ORD-ICN"
	);

	private final FlightOfferRepository flightOfferRepository;
	private final FlightPriceHistoryRepository priceHistoryRepository;

	public FlightFeatureVector calculate(
		String departureCode,
		String arrivalCode,
		LocalDate departureAt,
		CachedFlightResult cached
	) {
		LocalDateTime now = LocalDateTime.now(KST);
		String routeId = departureCode + "-" + arrivalCode;
		String searchedDayOfWeek = now.getDayOfWeek().name().substring(0, 3); // MON~SUN
		int daysToDeparture = (int) ChronoUnit.DAYS.between(now.toLocalDate(), departureAt);
		boolean isWeekendSearch = searchedDayOfWeek.equals("SAT") || searchedDayOfWeek.equals("SUN");
		boolean isLongHaul = LONG_HAUL_ROUTES.contains(routeId);

		// 2-2: 실시간 검색 결과에서 집계
		List<CachedFlightItem> flights = cached.flights();
		int offerCount = flights.size();

		int currentCheapestPrice = flights.stream()
			.mapToInt(CachedFlightItem::price)
			.min()
			.orElse(0);

		long nonstopCount = flights.stream().filter(CachedFlightItem::nonstop).count();
		float nonstopRatio = offerCount > 0 ? (float) nonstopCount / offerCount : 0f;

		OptionalInt cheapestNonstopOpt = flights.stream()
			.filter(CachedFlightItem::nonstop)
			.mapToInt(CachedFlightItem::price)
			.min();
		Integer cheapestNonstopPrice = cheapestNonstopOpt.isPresent() ? cheapestNonstopOpt.getAsInt() : null;

		boolean cheapestOfferHasLayover = flights.stream()
			.min(Comparator.comparingInt(CachedFlightItem::price))
			.map(item -> !item.nonstop())
			.orElse(false);

		Integer currGapToTypicalMin = cached.typicalPriceMin() != null
			? currentCheapestPrice - cached.typicalPriceMin() : null;
		Integer currGapToTypicalMax = cached.typicalPriceMax() != null
			? currentCheapestPrice - cached.typicalPriceMax() : null;

		// 2-3: trajectory 내 과거 FlightSearch별 최저 OUTBOUND 가격 시계열 (최근 3개, DESC)
		List<Integer> recentObs = flightOfferRepository.findRecentMinOutboundPrices(
			departureCode, arrivalCode, departureAt, FlightDirection.OUTBOUND,
			now, PageRequest.of(0, ROLLING_WINDOW));

		Float priceChange1 = !recentObs.isEmpty()
			? (float) (currentCheapestPrice - recentObs.get(0)) : null;
		Float rollingStd3 = recentObs.size() >= 2
			? computeStd(recentObs) : null;
		Float priceVsRollingMean3 = !recentObs.isEmpty()
			? currentCheapestPrice - computeMean(recentObs) : null;

		// 2-4: SerpAPI price_history (DB 저장값, 최근 N=5개, DESC → 역순으로 slope 계산)
		List<Integer> histPricesDesc = priceHistoryRepository.findLatestHistoryPrices(
			departureCode, arrivalCode, departureAt, now, PageRequest.of(0, HISTORY_WINDOW));
		List<Integer> histPrices = new ArrayList<>(histPricesDesc);
		Collections.reverse(histPrices);

		Float histRecentStd = histPrices.size() >= 2 ? computeStd(histPrices) : null;
		Float histRecentSlope = histPrices.size() >= 2 ? computeSlope(histPrices) : null;
		Float currVsHistMean = !histPrices.isEmpty()
			? currentCheapestPrice / computeMean(histPrices) : null;

		return new FlightFeatureVector(
			routeId, searchedDayOfWeek, daysToDeparture, isWeekendSearch, isLongHaul,
			offerCount, nonstopRatio, cheapestNonstopPrice, cheapestOfferHasLayover, currentCheapestPrice,
			currGapToTypicalMin, currGapToTypicalMax,
			histRecentStd, histRecentSlope, currVsHistMean,
			priceChange1, rollingStd3, priceVsRollingMean3
		);
	}

	private float computeMean(List<Integer> values) {
		return (float) values.stream().mapToInt(Integer::intValue).average().orElse(0);
	}

	private float computeStd(List<Integer> values) {
		float mean = computeMean(values);
		double variance = values.stream()
			.mapToDouble(v -> Math.pow(v - mean, 2))
			.average()
			.orElse(0);
		return (float) Math.sqrt(variance);
	}

	private float computeSlope(List<Integer> values) {
		int n = values.size();
		float sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
		for (int i = 0; i < n; i++) {
			sumX += i;
			sumY += values.get(i);
			sumXY += (float) i * values.get(i);
			sumX2 += (float) i * i;
		}
		float denom = n * sumX2 - sumX * sumX;
		return denom == 0 ? 0f : (n * sumXY - sumX * sumY) / denom;
	}
}
