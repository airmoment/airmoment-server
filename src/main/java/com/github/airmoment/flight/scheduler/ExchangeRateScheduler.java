package com.github.airmoment.flight.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.airmoment.global.client.frankfurter.ExchangeRateClient;
import com.github.airmoment.global.client.frankfurter.dto.FrankfurterResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeRateScheduler {

	private static final String REDIS_KEY_CURRENT = "exchange:rate:current";
	private static final String REDIS_KEY_7DAYS_AGO = "exchange:rate:7days-ago";
	private static final String CURRENCY = "KRW";

	private final ExchangeRateClient exchangeRateClient;
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	@Scheduled(cron = "0 0 0,12 * * *", zone = "Asia/Seoul")
	public void updateExchangeRate() {
		log.info("환율 데이터 수집 시작");
		try {
			LocalDate sevenDaysAgo = LocalDate.now(ZoneId.systemDefault()).minusDays(7);

			FrankfurterResponse current = exchangeRateClient.fetchLatest();
			FrankfurterResponse sevenDaysAgoRate = exchangeRateClient.fetchByDate(sevenDaysAgo);

			saveToRedis(REDIS_KEY_CURRENT, current);
			saveToRedis(REDIS_KEY_7DAYS_AGO, sevenDaysAgoRate);

			log.info("환율 데이터 업데이트 완료 - current: {} KRW, 7일전: {} KRW",
				current != null ? current.rate(CURRENCY) : "N/A",
				sevenDaysAgoRate != null ? sevenDaysAgoRate.rate(CURRENCY) : "N/A");

		} catch (Exception e) {
			log.error("환율 데이터 수집 실패: {}", e.getMessage());
		}
	}

	private void saveToRedis(String key, FrankfurterResponse response) throws JsonProcessingException {
		if (response == null || response.rate(CURRENCY) == null) {
			log.warn("환율 데이터가 없어 Redis 업데이트를 건너뜁니다. key: {}", key);
			return;
		}
		redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(response));
	}
}
