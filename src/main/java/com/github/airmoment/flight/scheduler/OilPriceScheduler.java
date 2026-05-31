package com.github.airmoment.flight.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.airmoment.flight.dto.DailyOilPrice;
import com.github.airmoment.global.client.eia.OilPriceClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OilPriceScheduler {

	private static final String REDIS_KEY = "oil:price:latest";
	private static final int FETCH_RANGE_DAYS = 7;

	private final OilPriceClient oilPriceClient;
	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	@Scheduled(cron = "0 0 0,12 * * *", zone = "Asia/Seoul")
	public void updateOilPrice() {
		log.info("유가 데이터 수집 시작");
		try {
			LocalDate today = LocalDate.now(ZoneId.systemDefault());
			List<DailyOilPrice> prices = oilPriceClient.fetchWtiRange(today.minusDays(FETCH_RANGE_DAYS), today);

			if (prices.isEmpty()) {
				log.warn("유가 데이터를 가져오지 못했습니다.");
				return;
			}

			DailyOilPrice latest = prices.stream()
				.max(Comparator.comparing(DailyOilPrice::date))
				.orElseThrow();

			redisTemplate.opsForValue().set(REDIS_KEY, objectMapper.writeValueAsString(latest));
			log.info("유가 데이터 업데이트 완료 - date: {}, price: {}", latest.date(), latest.price());

		} catch (JsonProcessingException e) {
			log.error("유가 데이터 직렬화 실패: {}", e.getMessage());
		} catch (Exception e) {
			log.error("유가 데이터 수집 실패: {}", e.getMessage());
		}
	}
}
