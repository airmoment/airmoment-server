package com.github.airmoment.flight.service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.airmoment.flight.dto.CachedFlightResult;
import com.github.airmoment.flight.dto.ExplainDto;
import com.github.airmoment.flight.dto.ExplainResponse;
import com.github.airmoment.flight.dto.FlightItemResponse;
import com.github.airmoment.flight.dto.ForecastRequest;
import com.github.airmoment.global.client.fastapi.AIServerClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional(readOnly = true)
public class FlightExplainService {

	/** 프롬프트에 포함할 최대 항공권 수 (토큰 절약을 위해 최저가순 상위 N개만) */
	private static final int MAX_FLIGHTS_IN_PROMPT = 10;

	private static final String SYSTEM_PROMPT = """
		너는 항공권 가격 예측 결과를 사용자에게 설명해주는 도우미야.
		아래에 가격 예측 방향(direction)과 절감/추가부담 예상액(KRW),
		모델이 그렇게 판단한 근거 목록(reasons),
		그리고 참고용으로 예측에 쓰인 피처(features)와 현재 조회된 항공권 목록(flights)이 주어져.

		각 근거를 자연스러운 한국어 문장으로 다듬어줘.
		가능하면 features 와 flights 의 구체적인 수치(가격, 항공사, 소요시간 등)를 활용해
		근거를 더 구체적이고 설득력 있게 만들어줘.
		
		또한, 가격 예측 방향과, 생성되는 근거의 방향이 align되도록 해줘.

		규칙:
		- features 와 flights 에 실제로 있는 정보만 활용하고, 없는 사실을 새로 지어내지 마.
		- 근거의 개수와 순서는 reasons 입력과 동일하게 유지해.
		- 숫자나 사실을 왜곡하지 마.
		- 각 문장은 한 문장이나 두 문장으로 간결하게 작성해.
		""";

	private final FlightForecastService flightForecastService;
	private final AIServerClient aiServerClient;
	private final ObjectMapper objectMapper;
	private final ChatClient chatClient;

	public FlightExplainService(
		FlightForecastService flightForecastService,
		AIServerClient aiServerClient,
		ObjectMapper objectMapper,
		ChatClient.Builder chatClientBuilder
	) {
		this.flightForecastService = flightForecastService;
		this.aiServerClient = aiServerClient;
		this.objectMapper = objectMapper;
		this.chatClient = chatClientBuilder.build();
	}

	public ExplainDto explain(
		String departureCode,
		String arrivalCode,
		LocalDate departureAt,
		CachedFlightResult cached,
		List<FlightItemResponse> flights
	) {
		ForecastRequest request = flightForecastService.buildForecastRequest(departureCode, arrivalCode, departureAt, cached);
		ExplainResponse raw = aiServerClient.explain(request);

		if (raw == null || raw.reasons() == null || raw.reasons().isEmpty()) {
			log.warn("AI 서버 explain 응답이 비어 있습니다 - route: {}", request.routeId());
			return new ExplainDto(List.of());
		}

		return refine(raw, request, flights);
	}

	private ExplainDto refine(ExplainResponse raw, ForecastRequest request, List<FlightItemResponse> flights) {
		String numberedReasons = IntStream.range(0, raw.reasons().size())
			.mapToObj(i -> (i + 1) + ". " + raw.reasons().get(i))
			.reduce((a, b) -> a + "\n" + b)
			.orElse("");

		ExplainDto refined = chatClient.prompt()
			.system(SYSTEM_PROMPT)
			.user(u -> u.text("""
				방향: {direction}
				절감/추가부담 예상액(KRW): {direction_amount}원

				근거 목록:
				{reasons}

				예측에 쓰인 피처(features):
				{features}

				현재 조회된 항공권 목록(flights):
				{flights}
				""")
				.param("direction", toKorean(raw.direction()))
				.param("direction_amount", raw.directionAmount())
				.param("reasons", numberedReasons)
				.param("features", toJson(request))
				.param("flights", buildFlightSummary(flights)))
			.call()
			.entity(ExplainDto.class);

		// LLM 응답이 비정상일 경우 원본 근거로 폴백
		if (refined == null || refined.reasons() == null || refined.reasons().isEmpty()) {
			log.warn("Spring AI explain 응답이 비어 있어 원본 근거로 폴백합니다.");
			return new ExplainDto(raw.reasons());
		}

		return refined;
	}

	private String toJson(ForecastRequest request) {
		try {
			return objectMapper.writeValueAsString(request);
		} catch (Exception e) {
			log.warn("ForecastRequest 직렬화 실패: {}", e.getMessage());
			return "{}";
		}
	}

	/**
	 * 항공권 목록을 최저가순 상위 N개 + 가격대 요약으로 압축한다.
	 * airlinePhoto(URL) 등 설명에 불필요한 필드는 제외한다.
	 */
	private String buildFlightSummary(List<FlightItemResponse> flights) {
		if (flights == null || flights.isEmpty()) {
			return "조회된 항공권 없음";
		}

		int total = flights.size();
		int minPrice = flights.stream().mapToInt(FlightItemResponse::price).min().orElse(0);
		int maxPrice = flights.stream().mapToInt(FlightItemResponse::price).max().orElse(0);

		String lines = flights.stream()
			.sorted(Comparator.comparingInt(FlightItemResponse::price))
			.limit(MAX_FLIGHTS_IN_PROMPT)
			.map(f -> String.format("- %s %s→%s, %d분, %,d원",
				f.airlineName(), f.departureTime(), f.arrivalTime(), f.duration(), f.price()))
			.collect(Collectors.joining("\n"));

		return String.format("총 %d개, 가격대 %,d~%,d원 (최저가순 상위 %d개)%n%s",
			total, minPrice, maxPrice, Math.min(total, MAX_FLIGHTS_IN_PROMPT), lines);
	}

	private String toKorean(String direction) {
		if (direction == null) {
			return "불명확";
		}
		return switch (direction.toLowerCase()) {
			case "up" -> "상승";
			case "down" -> "하락";
			default -> direction;
		};
	}
}
