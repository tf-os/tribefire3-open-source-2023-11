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
package com.braintribe.model.processing.aws.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.utils.IOTools;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest.Builder;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class ManualTest {

	@SuppressWarnings({ "null", "unused" })
	public static void main(String[] args) throws Exception {

		Integer httpConnectionPoolSize = 4096;
		SdkHttpClient httpClient = null;

		AwsBasicCredentials awsCreds = AwsBasicCredentials.create(AwsTestCredentials.getAccessKey(), AwsTestCredentials.getSecretAccessKey());
		StaticCredentialsProvider awsCredsProvider = StaticCredentialsProvider.create(awsCreds);

		S3ClientBuilder builder = S3Client.builder().region(Region.EU_CENTRAL_1).credentialsProvider(awsCredsProvider);

		if (httpConnectionPoolSize != null && httpConnectionPoolSize > 0) {
			httpClient = ApacheHttpClient.builder().maxConnections(httpConnectionPoolSize).build();
			builder.httpClient(httpClient);
		}
		S3Client s3Client = builder.build();

		int workers = 1;
		int iterations = 1;
		int totalExecs = workers * iterations;

		ExecutorService pool = Executors.newFixedThreadPool(workers);
		List<Future<?>> futures = new ArrayList<>();
		AtomicLong totalWaitMs = new AtomicLong(0);
		AtomicLong totalDurationMs = new AtomicLong(0);

		for (int w = 0; w < workers; ++w) {
			final int workerId = w;
			futures.add(pool.submit(() -> {
				for (int i = 0; i < iterations; ++i) {
					long start = System.nanoTime();
					File outFile = new File("/Users/roman/Downloads/0target/w-" + workerId + "-" + i + ".mp4");
					InputStream inStream = openStream(s3Client, "bt-vgn-prod",
							"2102/0420/46/720582eb-31f8-428f-bbca-bc0d397eb9e1/05_-_Su__kartoffel_Pommes.mp4", null, null);
					try (OutputStream os = new BufferedOutputStream(new FileOutputStream(outFile))) {
						long intermediate = System.nanoTime();
						long waitForConnection = (intermediate - start) / Numbers.NANOSECONDS_PER_MILLISECOND;
						totalWaitMs.addAndGet(waitForConnection);
						IOTools.pump(inStream, os);
					} catch (Exception e) {
						e.printStackTrace();
					}
					long stop = System.nanoTime();
					long durationMs = (stop - start) / Numbers.NANOSECONDS_PER_MILLISECOND;
					totalDurationMs.addAndGet(durationMs);
				}
			}));
		}

		for (Future<?> f : futures) {
			try {
				f.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (httpClient != null) {
			httpClient.close();
		}
		s3Client.close();
		pool.shutdownNow();

		System.out.println("Executions: " + totalExecs);
		System.out.println("Avg wait time: " + (((double) totalWaitMs.get()) / totalExecs) + " ms");
		System.out.println("Avg duration: " + (((double) totalDurationMs.get()) / totalExecs) + " ms");

	}

	private static InputStream openStream(S3Client client, String bucketName, String key, Long start, Long end) {

		Builder builder = GetObjectRequest.builder().bucket(bucketName).key(key);
		if (start != null && end != null) {
			builder.range("bytes=" + start + "-" + end);
		}
		GetObjectRequest request = builder.build();

		try {
			ResponseInputStream<GetObjectResponse> inputStream = client.getObject(request, ResponseTransformer.toInputStream());
			return inputStream;

		} catch (SdkException e) {
			throw Exceptions.unchecked(e, "AWS S3 exception at downloadFile");
		}
	}
}
