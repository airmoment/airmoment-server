package com.github.airmoment.interest.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.airmoment.flight.domain.enums.AirportCode;
import com.github.airmoment.global.exception.AirmomentException;
import com.github.airmoment.interest.domain.Interest;
import com.github.airmoment.interest.dto.BookmarkCreateRequest;
import com.github.airmoment.interest.dto.BookmarkCreateResponse;
import com.github.airmoment.interest.dto.EmailNotificationRequest;
import com.github.airmoment.interest.dto.InterestDto;
import com.github.airmoment.interest.exception.InterestErrorCode;
import com.github.airmoment.interest.repository.InterestRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InterestService {

	private final InterestRepository interestRepository;

	@Transactional
	public BookmarkCreateResponse createBookmark(Long memberId, BookmarkCreateRequest request) {
		Interest interest = findOrBuildInterest(memberId, request.departureCode(), request.arrivalCode(),
			request.departureAt(), request.nonstopOnly());

		if (Boolean.TRUE.equals(interest.getIsBookmarked())) {
			throw new AirmomentException(InterestErrorCode.INTEREST_ALREADY_BOOKMARKED);
		}

		interest.updateAsBookmarked();

		return new BookmarkCreateResponse(interestRepository.save(interest).getId());
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

	@Transactional
	public BookmarkCreateResponse enableEmailNotification(Long memberId, EmailNotificationRequest request) {
		Interest interest = findOrBuildInterest(memberId, request.departureCode(), request.arrivalCode(),
			request.departureAt(), request.nonstopOnly());

		if (Boolean.TRUE.equals(interest.getIsEmailNotificationEnabled())) {
			throw new AirmomentException(InterestErrorCode.EMAIL_NOTIFICATION_ALREADY_ENABLED);
		}

		interest.updateAsEmailNotificationEnabled();

		return new BookmarkCreateResponse(interestRepository.save(interest).getId());
	}

	@Transactional
	public void disableEmailNotification(Long memberId, Long interestId) {
		Interest interest = interestRepository.findById(interestId)
			.orElseThrow(() -> new AirmomentException(InterestErrorCode.INTEREST_NOT_FOUND));

		if (!Objects.equals(interest.getMemberId(), memberId)) {
			throw new AirmomentException(InterestErrorCode.ACCESS_DENIED);
		}

		if (!Boolean.TRUE.equals(interest.getIsEmailNotificationEnabled())) {
			throw new AirmomentException(InterestErrorCode.EMAIL_NOTIFICATION_ALREADY_DISABLED);
		}

		interest.updateAsEmailNotificationDisabled();
	}

	@Transactional(readOnly = true)
	public List<InterestDto> getInterests(Long memberId) {
		LinkedHashMap<Long, Interest> merged = new LinkedHashMap<>();

		getBookmarkedInterests(memberId).forEach(i -> merged.put(i.getId(), i));
		getEmailNotificationInterests(memberId).forEach(i -> merged.putIfAbsent(i.getId(), i));

		return merged.values().stream()
			.map(InterestDto::from)
			.toList();
	}

	private List<Interest> getBookmarkedInterests(Long memberId) {
		return interestRepository.findAllByMemberIdAndIsBookmarkedTrueOrderByCreatedAtDesc(memberId);
	}

	private List<Interest> getEmailNotificationInterests(Long memberId) {
		return interestRepository.findAllByMemberIdAndIsEmailNotificationEnabledTrueOrderByCreatedAtDesc(memberId);
	}

	private Interest findOrBuildInterest(
		Long memberId,
		AirportCode departureCode,
		AirportCode arrivalCode,
		LocalDate departureAt,
		boolean nonstopOnly) {

		return interestRepository
			.findByMemberIdAndDepartureCodeAndArrivalCodeAndDepartureAtAndNonstopOnly(
				memberId, departureCode, arrivalCode, departureAt, nonstopOnly)
			.orElseGet(() -> Interest.of(departureCode, arrivalCode, departureAt, nonstopOnly, memberId));
	}
}
