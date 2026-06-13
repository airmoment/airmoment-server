package com.github.airmoment.global.client.gmail;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailService {

	private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
	private static final String GMAIL_SEND_URL = "https://gmail.googleapis.com/gmail/v1/users/me/messages/send";

	private final GmailProperties gmailProperties;
	private final RestClient restClient;

	public void sendBuyNotification(String toEmail, String dep, String arr, LocalDate departureAt) {
		try {
			String accessToken = refreshAccessToken();
			String rawEmail = buildRawEmail(gmailProperties.senderEmail(), toEmail, dep, arr, departureAt);
			String encoded = Base64.getUrlEncoder().withoutPadding()
				.encodeToString(rawEmail.getBytes(StandardCharsets.UTF_8));

			restClient.post()
				.uri(GMAIL_SEND_URL)
				.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("raw", encoded))
				.retrieve()
				.toBodilessEntity();

			log.info("최저가 알림 이메일 전송 완료 - to: {}, route: {}-{}", toEmail, dep, arr);
		} catch (Exception e) {
			log.error("이메일 전송 실패 - to: {}, error: {}", toEmail, e.getMessage());
			throw new RuntimeException("이메일 전송 실패", e);
		}
	}

	@SuppressWarnings("unchecked")
	private String refreshAccessToken() {
		MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
		form.add("client_id", gmailProperties.clientId());
		form.add("client_secret", gmailProperties.clientSecret());
		form.add("refresh_token", gmailProperties.refreshToken());
		form.add("grant_type", "refresh_token");

		Map<String, Object> response = restClient.post()
			.uri(TOKEN_URL)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(form)
			.retrieve()
			.body(Map.class);

		if (response == null || !response.containsKey("access_token")) {
			throw new RuntimeException("액세스 토큰 갱신 실패");
		}
		return (String) response.get("access_token");
	}

	private String buildRawEmail(String from, String to, String dep, String arr, LocalDate departureAt) {
		String subject = "[에어모먼트] " + dep + " → " + arr + " 항공권 최저가 구매 알림";
		String encodedSubject = "=?UTF-8?B?" +
			Base64.getEncoder().encodeToString(subject.getBytes(StandardCharsets.UTF_8)) + "?=";

		String topImage = loadImageAsBase64("static/images/airmoment_top.png");
		String bottomImage = loadImageAsBase64("static/images/airmoment_bottom.png");

		String body = String.format("""
			<html><body style="margin:0;padding:0;background:#f5f5f5;font-family:sans-serif;">
			  <div style="max-width:600px;margin:0 auto;background:#ffffff;">
			    <img src="data:image/png;base64,%s" alt="header" style="width:100%%;display:block;">
			    <div style="padding:32px 40px;">
			      <p style="font-size:16px;color:#333;">안녕하세요!</p>
			      <p style="font-size:16px;color:#333;">
			        <strong>%s → %s</strong> (%s 출발) 항공권의 구매 시점이 도래했습니다.
			      </p>
			      <p style="font-size:16px;color:#333;">
			        AI 분석 결과, 현재가 <strong>구매 적기</strong>로 판단됩니다.
			      </p>
			      <p style="font-size:16px;color:#333;">지금 바로 에어모먼트에서 확인해보세요.</p>
			      <a href="https://airmoment-web.vercel.app/"
			         style="display:inline-block;margin-top:16px;padding:12px 28px;background:#1a73e8;color:#ffffff;font-size:15px;font-weight:bold;text-decoration:none;border-radius:6px;">
			        항공권 확인하기
			      </a>
			    </div>
			    <img src="data:image/png;base64,%s" alt="footer" style="width:100%%;display:block;">
			  </div>
			</body></html>
			""", topImage, dep, arr, departureAt, bottomImage);

		return "From: " + from + "\r\n"
			+ "To: " + to + "\r\n"
			+ "Content-Type: text/html; charset=UTF-8\r\n"
			+ "Subject: " + encodedSubject + "\r\n"
			+ "\r\n"
			+ body;
	}

	private String loadImageAsBase64(String classpathPath) {
		try (InputStream is = new ClassPathResource(classpathPath).getInputStream()) {
			return Base64.getEncoder().encodeToString(is.readAllBytes());
		} catch (Exception e) {
			log.warn("이미지 로드 실패 - path: {}, error: {}", classpathPath, e.getMessage());
			return "";
		}
	}
}
