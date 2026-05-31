package com.github.airmoment.global.client.eia.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.github.airmoment.flight.dto.DailyOilPrice;

import java.time.LocalDate;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EiaResponse(Inner response) {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record Inner(List<EiaDataPoint> data) {}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public record EiaDataPoint(String period, String value) {
		// 결측치(value=null) 또는 비영업일 데이터는 호출부에서 걸러냄
		public boolean isValid() {
			return period != null && value != null && !value.isBlank();
		}
		public DailyOilPrice toDomain() {
			return new DailyOilPrice(LocalDate.parse(period), Double.parseDouble(value));
		}
	}

	/** 외부 API 모양 → 도메인 객체 리스트 변환. 결측치/주말 갭은 제외. */
	public List<DailyOilPrice> toDailyPrices() {
		if (response == null || response.data == null) return List.of();
		return response.data.stream()
			.filter(EiaDataPoint::isValid)
			.map(EiaDataPoint::toDomain)
			.toList();
	}
}