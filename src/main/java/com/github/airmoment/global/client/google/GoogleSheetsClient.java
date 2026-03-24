package com.github.airmoment.global.client.google;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.springframework.stereotype.Component;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleSheetsClient {

	private final GoogleSheetsProperties properties;

	private Sheets getSheetsService() throws Exception {
		GoogleCredentials credentials = GoogleCredentials
			.fromStream(new ByteArrayInputStream(
				properties.credentialsJson().getBytes()))
			.createScoped(List.of(SheetsScopes.SPREADSHEETS));

		return new Sheets.Builder(
			GoogleNetHttpTransport.newTrustedTransport(),
			GsonFactory.getDefaultInstance(),
			new HttpCredentialsAdapter(credentials))
			.setApplicationName("airmoment")
			.build();
	}

	public void appendRows(String sheetName, List<List<Object>> rows) throws Exception {
		Sheets service = getSheetsService();

		ValueRange body = new ValueRange().setValues(rows);

		// INSERT_ROWS로 설정하였기 때문에 데이터를 덮어쓰지 않고 기존 데이터 아래에 행을 추가함.
		service.spreadsheets().values()
			.append(properties.spreadsheetId(), sheetName + "!A1", body)
			.setValueInputOption("RAW")
			.setInsertDataOption("INSERT_ROWS")
			.execute();

		log.info("{} 시트 데이터 추가 완료: {}행", sheetName, rows.size());
	}
}
