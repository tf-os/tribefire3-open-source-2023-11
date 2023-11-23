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
package com.braintribe.model.processing.resource.sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.sql.Date;
import java.time.Instant;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.internal.ArrayComparisonFailure;

import com.braintribe.common.db.DbTestSupport;
import com.braintribe.common.db.DbVendor;
import com.braintribe.model.access.IncrementalAccess;
import com.braintribe.model.cache.CacheControl;
import com.braintribe.model.cache.CacheOptions;
import com.braintribe.model.processing.resource.sql.common.ProcessorConfig;
import com.braintribe.model.processing.resource.sql.common.TestFile;
import com.braintribe.model.processing.resource.sql.wire.SqlBinaryProcessorTestWireModule;
import com.braintribe.model.processing.resource.sql.wire.contract.MainContract;
import com.braintribe.model.processing.session.api.managed.NotFoundException;
import com.braintribe.model.resource.CallStreamCapture;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resource.source.SqlSource;
import com.braintribe.model.resourceapi.persistence.DeleteBinary;
import com.braintribe.model.resourceapi.persistence.DeleteBinaryResponse;
import com.braintribe.model.resourceapi.persistence.StoreBinary;
import com.braintribe.model.resourceapi.persistence.StoreBinaryResponse;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;
import com.braintribe.model.resourceapi.stream.GetBinary;
import com.braintribe.model.resourceapi.stream.GetBinaryResponse;
import com.braintribe.model.resourceapi.stream.StreamBinary;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.condition.FingerprintMismatch;
import com.braintribe.model.resourceapi.stream.condition.ModifiedSince;
import com.braintribe.model.resourceapi.stream.condition.StreamCondition;
import com.braintribe.model.resourceapi.stream.range.StreamRange;
import com.braintribe.utils.CommonTools;
import com.braintribe.utils.IOTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;

/**
 * {@link JdbcSqlBinaryProcessor} tests.
 * 
 */
public class SqlBinaryProcessorTest implements BinaryProcessorTestConstants {

	public static final DbVendor DB_VENDOR = DbVendor.derby;

	protected static WireContext<MainContract> context;
	private static MainContract contract;

	private static TestFile testFile;
	private static String targetName;
	private static IncrementalAccess access;

	private String serviceId;
	private Resource resource;
	private ProcessorConfig processorConfig;

	@BeforeClass
	public static void beforeClass() throws Exception {
		DbTestSupport.startDerby();
		context = Wire.context(SqlBinaryProcessorTestWireModule.INSTANCE);
		contract = context.contract();
		testFile = contract.testFiles().get("docx");
		targetName = "uploaded-" + testFile.name();
		access = contract.access();
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (context != null)
			context.shutdown();

		DbTestSupport.shutdownDerby();
	}

	@Test
	public void testStore() throws Exception {
		serviceId = SERVICE_ID_SIMPLE;

		StoreBinaryResponse storeResponse = store(targetName);
		assertNotNull("response", storeResponse);

		resource = storeResponse.getResource();
		assertNotNull("response resource", resource);
		assertNotNull("response resource name", resource.getName());
		assertEquals("response resource name", targetName, resource.getName());
		assertTrue("source should have been SqlSource, but is: " + resource.getResourceSource(), resource.getResourceSource() instanceof SqlSource);

		GetBinaryResponse getResponse = get(null, null, null);
		try (InputStream in = getResponse.getResource().openStream()) {
			testFile.assertContentEquals(in);
		}
	}

	@Test(expected = NotFoundException.class)
	public void testDelete() throws Exception {
		prepareResource(SERVICE_ID_SIMPLE, null);

		deleteResource();

		get(null, null, null); // NotFoundException expected
	}

	// #############################################
	// ## . . . . . . . . Stream . . . . . . . . .##
	// #############################################

	@Test
	public void testStream() throws Exception {
		prepareResource(SERVICE_ID_SIMPLE, contract.simpleSqlBinaryProcessorConfig());

		stream(null, true, null, null);
	}

	@Test
	public void testStreamWithRanges() throws Exception {
		prepareResource(SERVICE_ID_SIMPLE, contract.simpleSqlBinaryProcessorConfig());

		streamWithRanges(0L, 1L);
		streamWithRanges(0L, -1L);
		streamWithRanges(0L, Long.MAX_VALUE);
		streamWithRanges(10L, 20L);
	}

	private void streamWithRanges(Long start, Long end) throws Exception {
		stream(null, true, start, end);
	}

	@Test
	public void testStreamWithConditions() throws Exception {
		prepareResource(SERVICE_ID_ENRICHING, contract.enrichingSqlBinaryProcessorConfig());

		streamWithCondition(FingerprintMismatch.create("non-matching-fingerprint"), true);
		streamWithCondition(FingerprintMismatch.create(resource.getMd5()), false);
		streamWithCondition(ModifiedSince.create(Date.from(Instant.now().minusSeconds(60))), true);
		streamWithCondition(ModifiedSince.create(Date.from(Instant.now().plusSeconds(60))), false);
	}

	private void streamWithCondition(StreamCondition condition, boolean matchingOrNoCondition) throws Exception {
		stream(condition, matchingOrNoCondition, null, null);
	}

	private void stream(StreamCondition condition, boolean matchingOrNoCondition, Long start, Long end) throws Exception {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		StreamBinaryResponse streamResponse = stream(out, condition, start, end);
		assertStream(out, streamResponse, matchingOrNoCondition, start, end);
	}

	private void assertStream(ByteArrayOutputStream out, StreamBinaryResponse response, boolean matchingOrNoCondition, Long start, Long end) {
		assertNotNull("no response", response);

		assertNotEquals("Wrong 'notStreamed' flag returned.", matchingOrNoCondition, response.getNotStreamed());
		byte[] outData = out.toByteArray();
		if (matchingOrNoCondition)
			assertRetrievedData(response, outData, start, end);
		else
			assertTrue(outData.length == 0);

		CacheControl cacheControl = response.getCacheControl();
		CacheOptions cacheOptions = processorConfig.getCacheOptions();

		assertNotNull("no response cache control", cacheControl);
		assertEquals("response cache control has wrong fingerprint", resource.getMd5(), cacheControl.getFingerprint());

		assertEquals("response cache control has wrong max age", Integer.valueOf(cacheOptions.getMaxAge()), cacheControl.getMaxAge());
		assertEquals("response cache control has wrong must revalidate", cacheOptions.getMustRevalidate(), cacheControl.getMustRevalidate());
		assertEquals("response cache control has wrong cache type", cacheOptions.getType(), cacheControl.getType());
	}

	// #############################################
	// ## . . . . . . . . . Get . . . . . . . . . ##
	// #############################################

	@Test
	public void testGet() throws Exception {
		prepareResource(SERVICE_ID_SIMPLE, contract.simpleSqlBinaryProcessorConfig());

		get(null, true, null, null);
	}

	@Test
	public void testGetWithRanges() throws Exception {
		prepareResource(SERVICE_ID_SIMPLE, contract.simpleSqlBinaryProcessorConfig());

		testGetWithRanges(0L, 10L);
		testGetWithRanges(0L, 1L);
		testGetWithRanges(0L, -1L);
		testGetWithRanges(0L, Long.MAX_VALUE);
		testGetWithRanges(10L, 20L);
	}

	private void testGetWithRanges(Long rangeStart, Long rangeEnd) throws Exception {
		get(null, true, rangeStart, rangeEnd);
	}

	@Test
	public void testGetWithConditions() throws Exception {
		prepareResource(SERVICE_ID_ENRICHING, contract.enrichingSqlBinaryProcessorConfig());

		getWithCondition(FingerprintMismatch.create("non-matching-fingerprint"), true);
		getWithCondition(FingerprintMismatch.create(resource.getMd5()), false);
		getWithCondition(ModifiedSince.create(Date.from(Instant.now().minusSeconds(60))), true);
		getWithCondition(ModifiedSince.create(Date.from(Instant.now().plusSeconds(60))), false);
	}

	private void getWithCondition(StreamCondition condition, boolean matchingOrNoCondition) throws Exception {
		get(condition, matchingOrNoCondition, null, null);
	}

	private void get(StreamCondition condition, boolean matchingOrNoCondition, Long start, Long end) throws Exception {
		GetBinaryResponse getResponse = get(condition, start, end);
		assertGet(getResponse, matchingOrNoCondition, start, end);
	}

	protected void assertGet(GetBinaryResponse response, boolean matchingOrNoCondition, Long start, Long end) {
		assertNotNull("no response", response);

		Resource responseResource = response.getResource();

		if (matchingOrNoCondition) {
			assertNotNull("Null or matching condition was given, it should have returned a resource", responseResource);
			byte[] responseData = bytesOf(responseResource);
			assertTrue("response resouce has no data", responseData != null && responseData.length > 0);
			assertRetrievedData(response, responseData, start, end);

		} else {
			assertNull("Non-matching condition was given, it shouldn't have returned a resource", responseResource);
		}

		CacheControl cacheControl = response.getCacheControl();
		CacheOptions cacheOptions = processorConfig.getCacheOptions();

		assertNotNull("no response cache control", cacheControl);
		assertEquals("response cache control has wrong fingerprint", resource.getMd5(), cacheControl.getFingerprint());

		assertEquals("response cache control has wrong max age", Integer.valueOf(cacheOptions.getMaxAge()), cacheControl.getMaxAge());
		assertEquals("response cache control has wrong must revalidate", cacheOptions.getMustRevalidate(), cacheControl.getMustRevalidate());
		assertEquals("response cache control has wrong cache type", cacheOptions.getType(), cacheControl.getType());
	}

	private byte[] bytesOf(Resource responseResource) {
		try (InputStream in = responseResource.openStream()) {
			return IOTools.slurpBytes(in);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void assertRetrievedData(BinaryRetrievalResponse response, byte[] retrievedData, Long start, Long end) throws ArrayComparisonFailure {
		if (start != null && end != null) {
			byte[] fullSource = testFile.contents();
			if (end <= 0 || end >= fullSource.length) {
				end = fullSource.length - 1L;
			}
			int len = (int) (end - start) + 1;
			byte[] rangedResult = new byte[len];
			System.arraycopy(fullSource, start.intValue(), rangedResult, 0, len);

			assertArrayEquals(testFile.name() + " contents are not equals", rangedResult, retrievedData);

			assertThat(response.getRangeStart()).isEqualTo(start);
			assertThat(response.getRangeEnd()).isEqualTo(end);
			assertThat(response.getSize()).isEqualTo(fullSource.length);

			if (response instanceof GetBinaryResponse) {
				Resource resource = ((GetBinaryResponse) response).getResource();
				assertThat(resource.getFileSize()).isEqualTo(len);
			}

		} else {
			testFile.assertContentEquals(retrievedData);
			assertThat(response.getRanged()).isFalse();
		}
	}

	private void prepareResource(String serviceId, ProcessorConfig processorConfig) throws Exception {
		this.serviceId = serviceId;
		this.processorConfig = processorConfig;

		StoreBinaryResponse storeResponse = store(null);
		resource = storeResponse.getResource();
	}

	private StoreBinaryResponse store(String targetName) throws Exception {
		String md5;
		try {
			// md5 is needed for testing fingerprint mismatch
			md5 = CommonTools.asString(IOTools.getMD5CheckSum(testFile.stream().openInputStream()));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		Resource createFrom = Resource.createTransient(testFile.stream());
		createFrom.setName(targetName == null ? testFile.name() : targetName);
		createFrom.setMd5(md5);
		createFrom.setCreated(new java.util.Date());

		StoreBinary store = StoreBinary.T.create();
		store.setCreateFrom(createFrom);
		store.setServiceId(serviceId);
		store.setDomainId(access.getAccessId());

		StoreBinaryResponse storeResponse = store.eval(contract.evaluator()).get();

		return storeResponse;
	}

	private DeleteBinaryResponse deleteResource() throws Exception {
		DeleteBinary delete = DeleteBinary.T.create();
		delete.setResource(resource);
		delete.setServiceId(serviceId);
		delete.setDomainId(access.getAccessId());

		return delete.eval(contract.evaluator()).get();
	}

	private StreamBinaryResponse stream(OutputStream out, StreamCondition condition, Long start, Long end) throws Exception {
		CallStreamCapture capture = CallStreamCapture.T.create();
		capture.setOutputStreamProvider(() -> out);

		StreamBinary stream = StreamBinary.T.create();
		stream.setResource(resource);
		stream.setCapture(capture);
		stream.setCondition(condition);
		stream.setServiceId(serviceId);
		stream.setDomainId(access.getAccessId());

		if (start != null && end != null) {
			StreamRange sr = StreamRange.T.create();
			sr.setStart(start);
			sr.setEnd(end);
			stream.setRange(sr);
		}

		return stream.eval(contract.evaluator()).get();
	}

	private GetBinaryResponse get(StreamCondition condition, Long rangeStart, Long rangeEnd) throws Exception {
		GetBinary get = GetBinary.T.create();
		get.setResource(resource);
		get.setCondition(condition);
		get.setServiceId(serviceId);
		get.setDomainId(access.getAccessId());

		if (rangeStart != null && rangeEnd != null) {
			StreamRange sr = StreamRange.T.create();
			sr.setStart(rangeStart);
			sr.setEnd(rangeEnd);
			get.setRange(sr);
		}

		return get.eval(contract.evaluator()).get();
	}

}
