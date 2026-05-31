package com.github.airmoment.interest.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.airmoment.global.response.dto.SuccessResponse;
import com.github.airmoment.interest.dto.MyPageResponse;
import com.github.airmoment.interest.service.MyPageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/mypage")
@RequiredArgsConstructor
public class MyPageController {

	private final MyPageService myPageService;

	@GetMapping
	public ResponseEntity<SuccessResponse<MyPageResponse>> getMyPage(
		@AuthenticationPrincipal UserDetails userDetails
	) {
		Long memberId = Long.parseLong(userDetails.getUsername());
		MyPageResponse response = myPageService.getMyPage(memberId);
		return ResponseEntity.ok(new SuccessResponse<>(200, "마이페이지 조회에 성공하였습니다.", response));
	}
}
