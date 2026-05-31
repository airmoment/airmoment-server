package com.github.airmoment.interest.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.github.airmoment.flight.dto.CachedFlightResult;
import com.github.airmoment.flight.dto.GraphResponse;
import com.github.airmoment.flight.service.FlightForecastService;
import com.github.airmoment.flight.service.FlightSearchService;
import com.github.airmoment.interest.dto.InterestDto;
import com.github.airmoment.interest.dto.MyPageInterestDto;
import com.github.airmoment.interest.dto.MyPageResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MyPageService {

	private final InterestService interestService;
	private final FlightSearchService flightSearchService;
	private final FlightForecastService flightForecastService;

	public MyPageResponse getMyPage(Long memberId) {
		List<InterestDto> interests = interestService.getInterests(memberId);

		List<MyPageInterestDto> items = interests.stream()
			.map(interest -> {
				GraphResponse graphResponse = null;
				try {
					String dep = interest.departureCode().name();
					String arr = interest.arrivalCode().name();
					CachedFlightResult cached = flightSearchService.getCachedOrFetchResult(dep, arr, interest.departureAt());
					graphResponse = flightForecastService.forecast(dep, arr, interest.departureAt(), cached);
				} catch (Exception e) {
					log.warn("노선 예측 실패 - {}-{} ({}): {}",
						interest.departureCode(), interest.arrivalCode(), interest.departureAt(), e.getMessage());
				}
				return MyPageInterestDto.of(interest, graphResponse);
			})
			.toList();

		return new MyPageResponse(items);
	}
}
