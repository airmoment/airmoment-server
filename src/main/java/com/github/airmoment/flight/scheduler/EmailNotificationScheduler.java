package com.github.airmoment.flight.scheduler;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.github.airmoment.flight.service.PredictionCacheService;
import com.github.airmoment.global.client.gmail.EmailService;
import com.github.airmoment.interest.domain.Interest;
import com.github.airmoment.interest.repository.InterestRepository;
import com.github.airmoment.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailNotificationScheduler {

	private final InterestRepository interestRepository;
	private final MemberRepository memberRepository;
	private final PredictionCacheService predictionCacheService;
	private final EmailService emailService;

	@Scheduled(cron = "0 0 0,12 * * *", zone = "Asia/Seoul")
	public void checkBuySignalAndNotify() {
		List<Interest> interests = interestRepository.findAllByIsEmailNotificationEnabledTrue();
		log.info("최저가 알림 스케줄러 시작 - 대상: {}건", interests.size());

		int notified = 0;
		for (Interest interest : interests) {
			try {
				Long memberId = interest.getMemberId();
				String prev = predictionCacheService.getPrediction(memberId, interest);
				String curr = predictionCacheService.updatePrediction(memberId, interest);

				if (curr == null) continue;

				if ("WAIT".equals(prev) && "BUY".equals(curr)) {
					memberRepository.findById(memberId).ifPresent(member -> {
						try {
							emailService.sendBuyNotification(
								member.getEmail(),
								interest.getDepartureCode().name(),
								interest.getArrivalCode().name(),
								interest.getDepartureAt()
							);
						} catch (Exception e) {
							log.warn("이메일 전송 실패 - memberId: {}, error: {}", memberId, e.getMessage());
						}
					});
					notified++;
				}
			} catch (Exception e) {
				log.warn("알림 처리 실패 - interestId: {}, error: {}", interest.getId(), e.getMessage());
			}
		}

		log.info("최저가 알림 스케줄러 완료 - 처리: {}건, 알림 발송: {}건", interests.size(), notified);
	}
}
