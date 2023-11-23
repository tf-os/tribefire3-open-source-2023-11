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
package com.braintribe.model.processing.etcd.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import com.braintribe.common.lcd.Numbers;
import com.braintribe.exception.Exceptions;
import com.braintribe.model.processing.etcd.service.wire.EtcdBinaryProcessorTestWireModule;
import com.braintribe.model.processing.etcd.service.wire.contract.MainContract;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.persistence.StoreBinary;
import com.braintribe.model.resourceapi.persistence.StoreBinaryResponse;
import com.braintribe.model.resourceapi.stream.StreamBinary;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.testing.category.SpecialEnvironment;
import com.braintribe.utils.StringTools;
import com.braintribe.utils.date.NanoClock;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

@Category(SpecialEnvironment.class)
public class EtcdBinaryProcessorTest {

	protected static WireContext<MainContract> context;
	private static MainContract contract;

	@BeforeClass
	public static void beforeClass() {
		context = Wire.context(EtcdBinaryProcessorTestWireModule.INSTANCE);
		contract = context.contract();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (context != null)
			context.shutdown();
	}
	
	@Test
	public void testStreaming() throws Exception {

		byte[] data = new byte[1024];
		new Random().nextBytes(data);

		StoreBinary request = StoreBinary.T.create();
		ResourceMock resource = new ResourceMock(data, "test.pdf", "application/pdf", new Date());
		request.setCreateFrom(resource);

		StoreBinaryResponse storeResponse = request.eval(contract.evaluator()).get();		
		Resource storedResource = storeResponse.getResource();

		// Now fetch
		StreamBinary streamRequest = StreamBinary.T.create();
		streamRequest.setResource(storedResource);
		CallStreamCaptureMock capture = new CallStreamCaptureMock();
		streamRequest.setCapture(capture);

		StreamBinaryResponse streamResponse = streamRequest.eval(contract.evaluator()).get();
		assertThat(streamResponse.getNotStreamed()).isTrue();
		
		byte[] readData = capture.getData();
		assertThat(readData).isEqualTo(data);
	}

	@Test
	public void testLargeFile() throws Exception {

		byte[] data = new byte[5 * (int) Numbers.MEGABYTE]; // Bigger than the configured chunk size
		new Random().nextBytes(data);

		StoreBinary request = StoreBinary.T.create();
		ResourceMock resource = new ResourceMock(data, "test.pdf", "application/pdf", new Date());
		request.setCreateFrom(resource);

		StoreBinaryResponse storeResponse = request.eval(contract.evaluator()).get();
		Resource storedResource = storeResponse.getResource();

		// Now fetch

		StreamBinary streamRequest = StreamBinary.T.create();
		streamRequest.setResource(storedResource);
		CallStreamCaptureMock capture = new CallStreamCaptureMock();
		streamRequest.setCapture(capture);

		StreamBinaryResponse streamBinaryResponse = streamRequest.eval(contract.evaluator()).get();
		assertThat(streamBinaryResponse.getNotStreamed()).isTrue();
		
		byte[] readData = capture.getData();

		assertThat(readData).isEqualTo(data);
	}

	@Test
	public void testMultiStreaming() throws Exception {

		int workers = 10;
		int iterations = 100;
		int dataSize = 100_000;

		ExecutorService service = Executors.newFixedThreadPool(workers);

		try {

			Instant start = NanoClock.INSTANCE.instant();

			List<Future<?>> futures = new ArrayList<>();
			for (int i = 0; i < workers; ++i) {
				final int workerId = i;
				futures.add(service.submit(() -> {

					long[] storeTimes = new long[iterations];
					long[] readTimes = new long[iterations];

					for (int j = 0; j < iterations; ++j) {

						byte[] data = new byte[dataSize];
						new Random().nextBytes(data);

						StoreBinary request = StoreBinary.T.create();
						ResourceMock resource = new ResourceMock(data, "test.pdf", "application/pdf", new Date());
						request.setCreateFrom(resource);

						long storeStart = System.nanoTime();
						StoreBinaryResponse storeResponse = request.eval(contract.evaluator()).get();
						storeTimes[j] = (System.nanoTime() - storeStart);

						Resource storedResource = storeResponse.getResource();

						// Now fetch

						StreamBinary streamRequest = StreamBinary.T.create();
						streamRequest.setResource(storedResource);
						CallStreamCaptureMock capture = new CallStreamCaptureMock();
						streamRequest.setCapture(capture);

						long readStart = System.nanoTime();
						StreamBinaryResponse streamBinaryResponse = streamRequest.eval(contract.evaluator()).get();
						assertThat(streamBinaryResponse.getNotStreamed()).isTrue();
						
						readTimes[j] = (System.nanoTime() - readStart);
						byte[] readData = capture.getData();
						assertThat(readData).isEqualTo(data);

					}

					Duration avgReadDuration = Duration.ofNanos(getAverage(readTimes));
					Duration avgWriteDuration = Duration.ofNanos(getAverage(storeTimes));
					System.out.println("Worker " + workerId + " done. Avg read time: " + StringTools.prettyPrintDuration(avgReadDuration, true, null)
							+ ", Avg write time: " + StringTools.prettyPrintDuration(avgWriteDuration, true, null));

				}));
			}

			futures.forEach(f -> {
				try {
					f.get();
				} catch (Exception e) {
					throw Exceptions.unchecked(e, "Error");
				}
			});

			System.out.println("Processed " + (workers * iterations) + " files a " + dataSize + " bytes in "
					+ StringTools.prettyPrintDuration(start, true, null));

		} finally {
			service.shutdown();
		}

	}

	protected static long getAverage(long[] values) {
		// Yeah, I know this is not safe, but it does the job and it is not likely that the max of Long is reached
		// anyway
		long total = 0L;
		for (int i = 0; i < values.length; ++i) {
			total += values[i];
		}
		return total / values.length;
	}
}
