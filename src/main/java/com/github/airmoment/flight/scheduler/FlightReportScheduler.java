package com.github.airmoment.flight.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.airmoment.flight.domain.FlightLayover;
import com.github.airmoment.flight.domain.FlightOffer;
import com.github.airmoment.flight.domain.FlightPriceHistory;
import com.github.airmoment.flight.domain.FlightPriceInsight;
import com.github.airmoment.flight.domain.FlightSearch;
import com.github.airmoment.flight.domain.FlightSegment;
import com.github.airmoment.flight.repository.FlightSearchRepository;
import com.github.airmoment.global.client.discord.DiscordClient;
import com.github.airmoment.global.client.google.GoogleSheetsClient;
import com.github.airmoment.global.client.google.GoogleSheetsProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlightReportScheduler {

	private final FlightSearchRepository flightSearchRepository;
	private final GoogleSheetsClient googleSheetsClient;
	private final DiscordClient discordClient;
	private final GoogleSheetsProperties sheetsProperties;

	@Scheduled(cron = "0 5 0 * * *", zone = "Asia/Seoul")  // 매일 00시
	public void reportDailyData() {
		log.info("일일 리포트 시작");
		try {
			// 오늘 하루의 시작과 끝
			LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
			LocalDateTime endOfDay = startOfDay.plusDays(1);

			List<FlightSearch> todaySearches = flightSearchRepository
				.findBySearchedAtBetween(startOfDay, endOfDay);

			if (todaySearches.isEmpty()) {
				log.info("오늘 수집된 데이터 없음");
				return;
			}

			// 각 시트별 데이터 추가
			appendFlightSearchSheet(todaySearches);
			appendFlightOfferSheet(todaySearches);
			appendFlightSegmentSheet(todaySearches);
			appendFlightLayoverSheet(todaySearches);
			appendFlightPriceInsightSheet(todaySearches);
			appendFlightPriceHistorySheet(todaySearches);

			// Discord 알림 전송
			String url = "https://docs.google.com/spreadsheets/d/"
				+ sheetsProperties.spreadsheetId();
			discordClient.sendMessage(
				"✈️ 오늘의 항공권 데이터를 수집하여 시트에 업데이트하였습니다.\n" +
				"⏰ 수집 일자 : " +  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시")) +
				"\n🔗 바로가기 : " + url
			);

			log.info("일일 리포트 완료");
		} catch (Exception e) {
			log.error("일일 리포트 실패: {}", e.getMessage());
		}
	}

	private void appendFlightSearchSheet(List<FlightSearch> searches) throws Exception {
		List<List<Object>> rows = new ArrayList<>();
		for (FlightSearch s : searches) {
			rows.add(List.of(
				s.getId(),
				s.getSearchedAt().toString(),
				s.getDepartureAirportCode(),
				s.getArrivalAirportCode(),
				s.getOutboundDate().toString(),
				"KRW"
			));
		}
		googleSheetsClient.appendRows("flight_search", rows);
	}

	private void appendFlightOfferSheet(List<FlightSearch> searches) throws Exception {
		List<List<Object>> rows = new ArrayList<>();

		for (FlightSearch s : searches) {
			for (FlightOffer o : s.getFlightOffers()) {
				rows.add(List.of(
					o.getId(),
					s.getId(),
					o.getPrice(),
					o.getTotalDuration(),
					o.getHasLayover(),
					o.getLayoverCount(),
					o.getIsBest(),
					o.getDirection().name()
				));
			}
		}
		googleSheetsClient.appendRows("flight_offer", rows);
	}

	private void appendFlightSegmentSheet(List<FlightSearch> searches) throws Exception {
		List<List<Object>> rows = new ArrayList<>();

		for (FlightSearch s : searches) {
			for (FlightOffer o : s.getFlightOffers()) {
				for (FlightSegment seg : o.getFlightSegments()) {
					rows.add(List.of(
						seg.getId(),
						o.getId(),
						seg.getSegmentOrder(),
						seg.getDepartureAirportCode(),
						nullSafe(seg.getDepartureAirportName()),
						nullSafe(seg.getDepartureTime()),
						seg.getArrivalAirportCode(),
						nullSafe(seg.getArrivalAirportName()),
						nullSafe(seg.getArrivalTime()),
						seg.getDuration(),
						nullSafe(seg.getAirline()),
						nullSafe(seg.getFlightNumber()),
						nullSafe(seg.getTravelClass()),
						nullSafe(seg.getLegroom()),
						nullSafe(seg.getAirplane())
					));
				}
			}
		}
		googleSheetsClient.appendRows("flight_segment", rows);
	}

	private void appendFlightLayoverSheet(List<FlightSearch> searches) throws Exception {
		List<List<Object>> rows = new ArrayList<>();

		for (FlightSearch s : searches) {
			for (FlightOffer o : s.getFlightOffers()) {
				for (FlightLayover l : o.getFlightLayovers()) {
					rows.add(List.of(
						l.getId(),
						o.getId(),
						l.getLayoverOrder(),
						l.getAirportCode(),
						nullSafe(l.getAirportName()),
						l.getDuration(),
						l.getIsOvernight()
					));
				}
			}
		}
		googleSheetsClient.appendRows("flight_layover", rows);
	}

	private void appendFlightPriceInsightSheet(List<FlightSearch> searches) throws Exception {
		List<List<Object>> rows = new ArrayList<>();

		for (FlightSearch s : searches) {
			if (s.getFlightPriceInsight() == null) continue;
			FlightPriceInsight i = s.getFlightPriceInsight();
			rows.add(List.of(
				i.getId(),
				s.getId(),
				nullSafe(i.getLowestPrice()),
				nullSafe(i.getPriceLevel()),
				nullSafe(i.getTypicalPriceMin()),
				nullSafe(i.getTypicalPriceMax())
			));
		}
		googleSheetsClient.appendRows("flight_price_insight", rows);
	}

	private void appendFlightPriceHistorySheet(List<FlightSearch> searches) throws Exception {
		List<List<Object>> rows = new ArrayList<>();

		for (FlightSearch s : searches) {
			for (FlightPriceHistory h : s.getFlightPriceHistories()) {
				rows.add(List.of(
					h.getId(),
					s.getId(),
					h.getTimeStamp(),
					h.getPrice()
				));
			}
		}
		googleSheetsClient.appendRows("flight_price_history", rows);
	}

	private Object nullSafe(Object value) {
		if (value == null) return "";
		if (value instanceof LocalDateTime dt) return dt.toString();
		if (value instanceof LocalDate d) return d.toString();
		return value;
	}
}
