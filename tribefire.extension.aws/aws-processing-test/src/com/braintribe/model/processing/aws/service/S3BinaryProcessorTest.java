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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Random;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.common.lcd.Pair;
import com.braintribe.model.aws.deployment.S3Region;
import com.braintribe.model.processing.aws.connect.S3ConnectionImpl;
import com.braintribe.model.processing.resource.server.test.commons.TestResourceAccessFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.persistence.StoreBinaryResponse;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.range.RangeResponse;
import com.braintribe.model.resourceapi.stream.range.StreamRange;
import com.braintribe.testing.tools.gm.GmTestTools;
import com.braintribe.utils.IOTools;

public class S3BinaryProcessorTest {

	private static S3ConnectionImpl connection;
	private final static String bucketName = "playground-rku";

	@BeforeClass
	public static void beforeClass() {
		com.braintribe.model.aws.deployment.S3Connector deployable = com.braintribe.model.aws.deployment.S3Connector.T.create();
		deployable.setRegion(S3Region.eu_central_1);
		deployable.setAwsAccessKey(AwsTestCredentials.getAccessKey());
		deployable.setAwsSecretAccessKey(AwsTestCredentials.getSecretAccessKey());

		connection = new S3ConnectionImpl();
		connection.setS3ConnectorDeployable(deployable);
		// connection.setHttpConnectionPoolSize(10);
	}

	@AfterClass
	public static void shutdown() throws Exception {
		connection.preDestroy();
	}

	@Test
	public void testBinaryProcessorGet() throws Exception {

		S3BinaryProcessorImpl processor = new S3BinaryProcessorImpl();
		processor.setS3Connection(connection);
		processor.setBucketName(bucketName);

		PersistenceGmSession session = GmTestTools.newSessionWithSmoodAccessMemoryOnly();
		TestResourceAccessFactory accessFactory = new TestResourceAccessFactory();
		((BasicPersistenceGmSession) session).setResourcesAccessFactory(accessFactory);

		byte[] source = new byte[1024];
		new Random().nextBytes(source);

		Resource storeResource = session.resources().create().name("test.bin").store(new ByteArrayInputStream(source));
		storeResource.setFileSize((long) source.length);

		StoreBinaryResponse response = processor.store(storeResource, session);

		Resource storedResource = response.getResource();

		assertThat(storedResource).isNotNull();

		Pair<byte[], GetBinaryResponse> result = downloadResourceGet(session, processor, storedResource, null, null);
		byte[] actual = result.first;

		assertThat(actual).isEqualTo(source);
		assertStreamRange(result.second, null, null, source);
	}

	@Test
	public void testBinaryProcessorGet0BytesLength() throws Exception {

		// Testing AD-1816

		S3BinaryProcessorImpl processor = new S3BinaryProcessorImpl();
		processor.setS3Connection(connection);
		processor.setBucketName(bucketName);

		PersistenceGmSession session = GmTestTools.newSessionWithSmoodAccessMemoryOnly();
		TestResourceAccessFactory accessFactory = new TestResourceAccessFactory();
		((BasicPersistenceGmSession) session).setResourcesAccessFactory(accessFactory);

		byte[] source = new byte[0];

		Resource storeResource = session.resources().create().name("test.bin").store(new ByteArrayInputStream(source));
		storeResource.setFileSize((long) source.length);

		StoreBinaryResponse response = processor.store(storeResource, session);

		Resource storedResource = response.getResource();

		assertThat(storedResource).isNotNull();

		Pair<byte[], GetBinaryResponse> result = downloadResourceGet(session, processor, storedResource, null, null);
		byte[] actual = result.first;

		assertThat(actual).isEqualTo(source);
		assertStreamRange(result.second, null, null, source);
	}

	@Test
	public void testBinaryProcessorGetWithRanges() throws Exception {

		S3BinaryProcessorImpl processor = new S3BinaryProcessorImpl();
		processor.setS3Connection(connection);
		processor.setBucketName(bucketName);

		PersistenceGmSession session = GmTestTools.newSessionWithSmoodAccessMemoryOnly();
		TestResourceAccessFactory accessFactory = new TestResourceAccessFactory();
		((BasicPersistenceGmSession) session).setResourcesAccessFactory(accessFactory);

		byte[] source = new byte[1024];
		new Random().nextBytes(source);

		Resource storeResource = session.resources().create().name("test.bin").store(new ByteArrayInputStream(source));
		storeResource.setFileSize((long) source.length);

		StoreBinaryResponse response = processor.store(storeResource, session);

		Resource storedResource = response.getResource();

		assertThat(storedResource).isNotNull();

		Pair<byte[], GetBinaryResponse> result = downloadResourceGet(session, processor, storedResource, 0L, 10L);
		byte[] actual = result.first;
		assertThat(actual).isEqualTo(getByteRange(source, 0, 10));
		assertStreamRange(result.second, 0L, 10L, source);

		result = downloadResourceGet(session, processor, storedResource, 0L, 65000L);
		byte[] actualRangeTooLarge = result.first;
		assertThat(actualRangeTooLarge).isEqualTo(getByteRange(source, 0, source.length));
		assertStreamRange(result.second, 0L, 65000L, source);

		result = downloadResourceGet(session, processor, storedResource, 0L, null);
		byte[] actualZeroToNull = result.first;
		assertThat(actualZeroToNull).isEqualTo(getByteRange(source, 0, source.length));
		assertStreamRange(result.second, 0L, null, source);

		result = downloadResourceGet(session, processor, storedResource, 10L, null);
		byte[] actualTenToNull = result.first;
		assertThat(actualTenToNull).isEqualTo(getByteRange(source, 10, source.length));
		assertStreamRange(result.second, 10L, null, source);

		result = downloadResourceGet(session, processor, storedResource, 5L, 7L);
		byte[] actualMiddle = result.first;
		assertThat(actualMiddle).isEqualTo(getByteRange(source, 5, 7));
		assertStreamRange(result.second, 5L, 7L, source);

	}

	@Test
	public void testBinaryProcessorStream() throws Exception {

		S3BinaryProcessorImpl processor = new S3BinaryProcessorImpl();
		processor.setS3Connection(connection);
		processor.setBucketName(bucketName);

		PersistenceGmSession session = GmTestTools.newSessionWithSmoodAccessMemoryOnly();
		TestResourceAccessFactory accessFactory = new TestResourceAccessFactory();
		((BasicPersistenceGmSession) session).setResourcesAccessFactory(accessFactory);

		byte[] source = new byte[1024];
		new Random().nextBytes(source);

		Resource storeResource = session.resources().create().name("test.bin").store(new ByteArrayInputStream(source));
		storeResource.setFileSize((long) source.length);

		StoreBinaryResponse response = processor.store(storeResource, session);

		Resource storedResource = response.getResource();

		assertThat(storedResource).isNotNull();

		Pair<byte[], StreamBinaryResponse> result = downloadResourceStream(session, processor, storedResource, null, null);
		byte[] actual = result.first;

		assertThat(actual).isEqualTo(source);
		assertStreamRange(result.second, null, null, source);

	}

	@Test
	public void testBinaryProcessorStreamWithRanges() throws Exception {

		S3BinaryProcessorImpl processor = new S3BinaryProcessorImpl();
		processor.setS3Connection(connection);
		processor.setBucketName(bucketName);

		PersistenceGmSession session = GmTestTools.newSessionWithSmoodAccessMemoryOnly();
		TestResourceAccessFactory accessFactory = new TestResourceAccessFactory();
		((BasicPersistenceGmSession) session).setResourcesAccessFactory(accessFactory);

		byte[] source = new byte[1024];
		new Random().nextBytes(source);

		Resource storeResource = session.resources().create().name("test.bin").store(new ByteArrayInputStream(source));
		storeResource.setFileSize((long) source.length);

		StoreBinaryResponse response = processor.store(storeResource, session);

		Resource storedResource = response.getResource();

		assertThat(storedResource).isNotNull();

		Pair<byte[], StreamBinaryResponse> result = downloadResourceStream(session, processor, storedResource, 0L, 10L);
		byte[] actual = result.first;
		assertThat(actual).isEqualTo(getByteRange(source, 0, 10));
		assertStreamRange(result.second, 0L, 10L, source);

		result = downloadResourceStream(session, processor, storedResource, 0L, 65000L);
		byte[] actualRangeTooLarge = result.first;
		assertThat(actualRangeTooLarge).isEqualTo(getByteRange(source, 0, source.length));
		assertStreamRange(result.second, 0L, 65000L, source);

		result = downloadResourceStream(session, processor, storedResource, 0L, null);
		byte[] actualZeroToNull = result.first;
		assertThat(actualZeroToNull).isEqualTo(getByteRange(source, 0, source.length));
		assertStreamRange(result.second, 0L, null, source);

		result = downloadResourceStream(session, processor, storedResource, 10L, null);
		byte[] actualTenToNull = result.first;
		assertThat(actualTenToNull).isEqualTo(getByteRange(source, 10, source.length));
		assertStreamRange(result.second, 10L, null, source);

		result = downloadResourceStream(session, processor, storedResource, 5L, 7L);
		byte[] actualMiddle = result.first;
		assertThat(actualMiddle).isEqualTo(getByteRange(source, 5, 7));
		assertStreamRange(result.second, 5L, 7L, source);

	}

	private void assertStreamRange(RangeResponse rangeResponse, Long startInclusive, Long endInclusive, byte[] source) {
		if (startInclusive == null) {
			assertThat(rangeResponse.getRanged()).isEqualTo(false);
			assertThat(rangeResponse.getRangeStart()).isNull();
			assertThat(rangeResponse.getRangeEnd()).isNull();
			assertFileSize(rangeResponse, source.length);
			return;
		}

		long expectedStart = startInclusive;
		long expectedEnd = -1;
		if (endInclusive == null) {
			expectedEnd = source.length - 1;
		} else {
			if (endInclusive >= source.length) {
				expectedEnd = source.length - 1;
			} else {
				expectedEnd = endInclusive;
			}
		}

		Long actualRangeStart = rangeResponse.getRangeStart();
		Long actualRangeEnd = rangeResponse.getRangeEnd();
		Long actualSize = rangeResponse.getSize();
		assertThat(actualRangeStart).isEqualTo(expectedStart);
		assertThat(actualRangeEnd).isEqualTo(expectedEnd);
		assertThat(actualSize).isEqualTo(source.length);

		assertFileSize(rangeResponse, actualRangeEnd - actualRangeStart + 1);
	}

	private void assertFileSize(RangeResponse rangeResponse, long size) {
		if (rangeResponse instanceof GetBinaryResponse) {
			Resource resource = ((GetBinaryResponse) rangeResponse).getResource();
			assertThat(resource.getFileSize()).isEqualTo(size);
		}
	}

	protected Pair<byte[], GetBinaryResponse> downloadResourceGet(PersistenceGmSession session, S3BinaryProcessorImpl processor,
			Resource storedResource, Long rangeStart, Long rangeEnd) throws Exception {

		StreamRange range = null;
		if (rangeStart != null) {
			range = session.create(StreamRange.T);
			range.setStart(rangeStart);
			range.setEnd(rangeEnd);
		}

		GetBinaryResponse response = GetBinaryResponse.T.create();
		GetBinaryResponse getResponse = processor.get(storedResource, range, response);
		Resource getResource = getResponse.getResource();

		try (InputStream in = getResource.openStream()) {
			byte[] actual = IOTools.slurpBytes(in);
			return new Pair<>(actual, getResponse);
		}
	}

	protected Pair<byte[], StreamBinaryResponse> downloadResourceStream(PersistenceGmSession session, S3BinaryProcessorImpl processor,
			Resource storedResource, Long rangeStart, Long rangeEnd) throws Exception {

		StreamRange range = null;
		if (rangeStart != null) {
			range = session.create(StreamRange.T);
			range.setStart(rangeStart);
			range.setEnd(rangeEnd);
		}

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		StreamBinaryResponse response = StreamBinaryResponse.T.create();
		StreamBinaryResponse streamResponse = processor.stream(storedResource, () -> out, range, null, response);
		assertThat(streamResponse).isNotNull();

		byte[] actual = out.toByteArray();
		return new Pair<>(actual, streamResponse);
	}

	protected byte[] getByteRange(byte[] source, int start, int endInclusive) {
		if (endInclusive >= source.length) {
			endInclusive = source.length - 1;
		}
		byte[] result = new byte[endInclusive - start + 1];
		System.arraycopy(source, start, result, 0, endInclusive - start + 1);
		return result;
	}

	@Test
	public void testKeySanitizer() throws Exception {

		S3BinaryProcessorImpl p = new S3BinaryProcessorImpl();
		assertThat(p.sanitize("hello.txt", 1024)).isEqualTo("hello.txt");
		assertThat(p.sanitize("hello world.txt", 1024)).isEqualTo("hello_world.txt");
		assertThat(p.sanitize("hello&world.txt", 1024)).isEqualTo("hello_world.txt");
		assertThat(p.sanitize("hello$world.txt", 1024)).isEqualTo("hello_world.txt");
		assertThat(p.sanitize("helloäworld.txt", 1024)).isEqualTo("hello_world.txt");
		assertThat(p.sanitize("hello\u0000world.txt", 1024)).isEqualTo("hello_world.txt");
		assertThat(p.sanitize("hello, world.txt", 1024)).isEqualTo("hello__world.txt");
		assertThat(p.sanitize("hello@world.txt", 1024)).isEqualTo("hello_world.txt");
		assertThat(p.sanitize("hello=world.txt", 1024)).isEqualTo("hello_world.txt");
		assertThat(p.sanitize("hello;world.txt", 1024)).isEqualTo("hello_world.txt");
		assertThat(p.sanitize("hello:world.txt", 1024)).isEqualTo("hello_world.txt");
		assertThat(p.sanitize("hello+world.txt", 1024)).isEqualTo("hello_world.txt");
		assertThat(p.sanitize("hello*world.txt", 1024)).isEqualTo("hello_world.txt");
		assertThat(p.sanitize("hello?world.txt", 1024)).isEqualTo("hello_world.txt");
		assertThat(p.sanitize("hello#world.txt", 1024)).isEqualTo("hello_world.txt");
		assertThat(p.sanitize("hello[]world.txt", 1024)).isEqualTo("hello__world.txt");
		assertThat(p.sanitize("你好世界.txt", 1024)).isEqualTo("____.txt");
		assertThat(p.sanitize("DE-MON-0112-1120_Data Protection Declaration.docx", 1024))
				.isEqualTo("DE-MON-0112-1120_Data_Protection_Declaration.docx");

	}
}
