package com.github.airmoment.interest.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.airmoment.global.response.dto.SuccessResponse;
import com.github.airmoment.interest.dto.BookmarkCreateRequest;
import com.github.airmoment.interest.dto.BookmarkCreateResponse;
import com.github.airmoment.interest.dto.EmailNotificationRequest;
import com.github.airmoment.interest.exception.InterestSuccessCode;
import com.github.airmoment.interest.service.InterestService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/v1/interests")
@RestController
@RequiredArgsConstructor
public class InterestController {

	private final InterestService interestService;


	@PostMapping("/bookmark")
	public ResponseEntity<SuccessResponse<BookmarkCreateResponse>> createBookmark(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid  @RequestBody BookmarkCreateRequest request
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		BookmarkCreateResponse response = interestService.createBookmark(memberId, request);

		return ResponseEntity.ok(SuccessResponse.of(InterestSuccessCode.BOOKMARK_CREATED, response));
	}

	@DeleteMapping("/bookmark/{interestId}")
	public ResponseEntity<SuccessResponse<Void>> deleteBookmark(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Long interestId
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		interestService.deleteBookmark(memberId, interestId);

		return ResponseEntity.ok(SuccessResponse.of(InterestSuccessCode.BOOKMARK_DELETED));
	}

	@PostMapping("/email-notification")
	public ResponseEntity<SuccessResponse<BookmarkCreateResponse>> enableEmailNotification(
		@AuthenticationPrincipal UserDetails userDetails,
		@Valid @RequestBody EmailNotificationRequest request
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		BookmarkCreateResponse response = interestService.enableEmailNotification(memberId, request);

		return ResponseEntity.ok(SuccessResponse.of(InterestSuccessCode.EMAIL_NOTIFICATION_ENABLED, response));
	}

	@DeleteMapping("/email-notification/{interestId}")
	public ResponseEntity<SuccessResponse<Void>> disableEmailNotification(
		@AuthenticationPrincipal UserDetails userDetails,
		@PathVariable Long interestId
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		interestService.disableEmailNotification(memberId, interestId);

		return ResponseEntity.ok(SuccessResponse.of(InterestSuccessCode.EMAIL_NOTIFICATION_DISABLED));
	}
}
