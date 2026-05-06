package com.github.airmoment.interest.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.airmoment.flight.domain.enums.AirportCode;
import com.github.airmoment.global.exception.AirmomentException;
import com.github.airmoment.interest.domain.Interest;
import com.github.airmoment.interest.dto.BookmarkCreateRequest;
import com.github.airmoment.interest.dto.BookmarkCreateResponse;
import com.github.airmoment.interest.dto.BookmarkDto;
import com.github.airmoment.interest.exception.InterestErrorCode;
import com.github.airmoment.interest.repository.InterestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InterestService {

	private final InterestRepository interestRepository;

	@Transactional
	public BookmarkCreateResponse createBookmark(Long memberId, BookmarkCreateRequest request) {
		Interest interest = createOrGetInterest(memberId, request.departureCode(), request.arrivalCode(),
			request.departureAt(), request.nonstopOnly());

		if (Boolean.TRUE.equals(interest.getIsBookmarked())) {
			throw new AirmomentException(InterestErrorCode.INTEREST_ALREADY_BOOKMARKED);
		}

		interest.updateAsBookmarked();

		return new BookmarkCreateResponse(interest.getId());
	}

	@Transactional
	public void deleteBookmark(Long memberId, Long interestId) {
		Interest interest = interestRepository.findById(interestId)
			.orElseThrow(() -> new AirmomentException(InterestErrorCode.INTEREST_NOT_FOUND));

		if (!Objects.equals(interest.getMemberId(), memberId)) {
			throw new AirmomentException(InterestErrorCode.ACCESS_DENIED);
		}

		if (!Boolean.TRUE.equals(interest.getIsBookmarked())) {
			throw new AirmomentException(InterestErrorCode.INTEREST_ALREADY_UNBOOKMARKED);
		}

		interest.updateAsUnbookmarked();
	}

	private Interest createOrGetInterest(
		Long memberId,
		AirportCode departureCode,
		AirportCode arrivalCode,
		LocalDate departureAt,
		boolean nonstopOnly) {

		return interestRepository
			.findByMemberIdAndDepartureCodeAndArrivalCodeAndDepartureAtAndNonstopOnly(
				memberId, departureCode, arrivalCode, departureAt, nonstopOnly)
			.orElseGet(() -> interestRepository.save(
				Interest.of(departureCode, arrivalCode, departureAt, nonstopOnly, memberId)));
	}

	private List<BookmarkDto> getBookmarksOrderByDesc(Long memberId) {
		return interestRepository.findAllByMemberIdAndIsBookmarkedTrueOrderByCreatedAtDesc(memberId)
			.stream()
			.map(interest -> BookmarkDto.from(
				interest.getDepartureCode(),
				interest.getArrivalCode(),
				interest.getDepartureAt(),
				interest.isNonstopOnly(),
				Boolean.TRUE.equals(interest.getIsBookmarked()),
				Boolean.TRUE.equals(interest.getIsEmailNotificationEnabled())
			))
			.toList();
	}
}
