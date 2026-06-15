package com.github.airmoment.flight.service;

import java.time.Duration;
import java.time.LocalDate;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.github.airmoment.flight.dto.AIPredictionResponse;
import com.github.airmoment.flight.dto.CachedFlightResult;
import com.github.airmoment.flight.dto.FlightFeatureVector;
import com.github.airmoment.global.client.fastapi.AIServerClient;
import com.github.airmoment.interest.domain.Interest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionCacheService {

	private static final String KEY_FORMAT = "predict:decision:%d:%s:%s:%s:%b";
	private static final Duration TTL = Duration.ofDays(1);

	private final RedisTemplate<String, String> redisTemplate;
	private final FlightSearchService flightSearchService;
	private final FlightFeatureService flightFeatureService;
	private final AIServerClient aiServerClient;

	public void saveInitialPrediction(Long memberId, Interest interest) {
		String decision = callPredict(interest);
		if (decision != null) {
			redisTemplate.opsForValue().set(buildKey(memberId, interest), decision, TTL);
			log.info("초기 예측 저장 - memberId: {}, route: {}-{}, decision: {}",
				memberId, interest.getDepartureCode(), interest.getArrivalCode(), decision);
		}
	}

	public String getPrediction(Long memberId, Interest interest) {
		return redisTemplate.opsForValue().get(buildKey(memberId, interest));
	}

	public String updatePrediction(Long memberId, Interest interest) {
		String decision = callPredict(interest);
		if (decision != null) {
			redisTemplate.opsForValue().set(buildKey(memberId, interest), decision, TTL);
		}
		return decision;
	}

	public void deletePrediction(Long memberId, Interest interest) {
		redisTemplate.delete(buildKey(memberId, interest));
	}

	private String buildKey(Long memberId, Interest interest) {
		return String.format(KEY_FORMAT,
			memberId,
			interest.getDepartureCode().name(),
			interest.getArrivalCode().name(),
			interest.getDepartureAt(),
			interest.isNonstopOnly());
	}

	private String callPredict(Interest interest) {
		try {
			String dep = interest.getDepartureCode().name();
			String arr = interest.getArrivalCode().name();
			LocalDate date = interest.getDepartureAt();

			CachedFlightResult cached = flightSearchService.getCachedOrFetchResult(dep, arr, date);
			FlightFeatureVector fv = flightFeatureService.calculate(dep, arr, date, cached);
			AIPredictionResponse resp = aiServerClient.predict(fv);
			return resp != null ? resp.decision() : null;
		} catch (Exception e) {
			log.warn("예측 호출 실패 - interestId: {}, error: {}", interest.getId(), e.getMessage());
			return null;
		}
	}
}
