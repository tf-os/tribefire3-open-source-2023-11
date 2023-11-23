// ============================================================================
// Copyright BRAINTRIBE TECHNOLOGY GMBH, Austria, 2002-2022
// 
// This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either version 3 of the License, or (at your option) any later version.
// 
// This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public License along with this library; See http://www.gnu.org/licenses/.
// ============================================================================
package tribefire.extension.antivirus.service.connector.virustotal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import com.google.gson.Gson;


public class VirusTotalScanner {

	private static final String URI_VT2_FILE_SCAN = "https://www.virustotal.com/vtapi/v2/file/scan";
	private static final String URI_VT2_FILE_SCAN_REPORT = "https://www.virustotal.com/vtapi/v2/file/report";

	private String apiKey;

	private Gson gsonProcessor;

	public VirusTotalScanner(String apiKey) {
		this.apiKey = apiKey;
		this.gsonProcessor = new Gson();
	}

	public ScanResult scanFile(InputStream inputStream) throws IOException {
		ScanResult scanResult = new ScanResult();
		CloseableHttpClient httpClient = HttpClients.createDefault();

		try {
			HttpPost request = new HttpPost(URI_VT2_FILE_SCAN);

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addBinaryBody("file", inputStream);
			builder.addTextBody("apikey", apiKey);

			HttpEntity postEntity = builder.build();
			request.setEntity(postEntity);

			CloseableHttpResponse response = httpClient.execute(request);

			try {
				// Get HttpResponse Status
				// System.out.println(response.getProtocolVersion()); // HTTP/1.1
				// System.out.println(response.getStatusLine().getStatusCode()); // 200
				// System.out.println(response.getStatusLine().getReasonPhrase()); // OK
				// System.out.println(response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					// return it as a String
					try (BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {

						String serviceResponse = br.lines().collect(Collectors.joining());
						scanResult = gsonProcessor.fromJson(serviceResponse, ScanResult.class);
					}
				}

			} finally {
				response.close();
			}
		} finally {
			httpClient.close();
		}
		return scanResult;
	}

	public FileScanReport getScanReport(String resource) throws IOException {
		CloseableHttpClient httpClient = HttpClients.createDefault();
		FileScanReport fileScanReport = new FileScanReport();

		try {
			HttpPost request = new HttpPost(URI_VT2_FILE_SCAN_REPORT);

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addTextBody("resource", resource);
			builder.addTextBody("apikey", apiKey);

			HttpEntity postEntity = builder.build();
			request.setEntity(postEntity);

			CloseableHttpResponse response = httpClient.execute(request);

			try {
				// Get HttpResponse Status
				// System.out.println(response.getProtocolVersion()); // HTTP/1.1
				// System.out.println(response.getStatusLine().getStatusCode()); // 200
				// System.out.println(response.getStatusLine().getReasonPhrase()); // OK
				// System.out.println(response.getStatusLine().toString()); // HTTP/1.1 200 OK

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					try (BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
						String serviceResponse = br.lines().collect(Collectors.joining());

						fileScanReport = gsonProcessor.fromJson(serviceResponse, FileScanReport.class);
					}
				}

			} finally {
				response.close();
			}
		} finally {
			httpClient.close();
		}
		return fileScanReport;
	}

}
