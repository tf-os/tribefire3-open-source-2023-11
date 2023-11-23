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
package com.braintribe.model.processing.resource.streaming.access.test;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.codec.marshaller.api.MarshallerRegistryEntry;
import com.braintribe.logging.Logger;
import com.braintribe.model.processing.resource.streaming.access.RemoteResourceAccess;
import com.braintribe.model.processing.resource.streaming.access.RemoteResourceAccessFactory;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.api.resource.ResourceRetrieveBuilder;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.stream.BinaryRetrievalResponse;
import com.braintribe.model.resourceapi.stream.StreamBinaryResponse;
import com.braintribe.model.resourceapi.stream.condition.FingerprintMismatch;
import com.braintribe.processing.test.web.undertow.UndertowServer;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.stream.api.StreamPipes;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.form.FormData;
import io.undertow.server.handlers.form.FormData.FormValue;
import io.undertow.server.handlers.form.FormDataParser;
import io.undertow.server.handlers.form.MultiPartParserDefinition;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

/**
 * <p>
 * {@link RemoteResourceAccess} test suite.
 * 
 * <p>
 * This suite manages an instance of Undertow embedded web server.
 * 
 */
public class RemoteResourceAccessTest {

	private static final Logger log = Logger.getLogger(RemoteResourceAccessTest.class);

	protected static int THREAD_POOL_SIZE = 10;
	protected static int MAX_CONCURRENT_TESTS = 10;
	protected static long CONCURRENT_TESTS_TIMEOUT = 25;
	protected static TimeUnit CONCURRENT_TESTS_TIMEOUT_UNIT = TimeUnit.SECONDS;
	protected static int MAX_SEQUENTIAL_TESTS = 10;
	protected static int TEST_DATA_SIZE = 20 * 1024;

	private static UndertowServer undertowServer;
	private static URL serverUrl;
	private static Marshaller marshaller;
	private static MarshallerRegistry marshallerRegistry;
	private static Map<String, TestResource> resources = new ConcurrentHashMap<>();

	private RemoteResourceAccessFactory remoteResourceAccessFactory;
	private TestAuthorizationContext authContext;

	@Rule
	public Timeout globalTimeout = Timeout.seconds(120);

	// ============================= //
	// ======== LIFE CYCLE ========= //
	// ============================= //

	@BeforeClass
	public static void startServer() throws Exception {

		undertowServer = UndertowServer.create("/test-app").addServlet("streamer", new TestServlet(), "/stream").start();
		serverUrl = undertowServer.getServletUrl("streamer");

		marshaller = new com.braintribe.codec.marshaller.stax.StaxMarshaller();
		marshallerRegistry = createMarshallerRegistry(marshaller, "application/xml");

	}

	@AfterClass
	public static void shutdownServer() throws Exception {
		undertowServer.stop();
	}

	@Before
	public void initializeFactory() throws Exception {

		authContext = new TestAuthorizationContext();

		remoteResourceAccessFactory = new RemoteResourceAccessFactory();
		remoteResourceAccessFactory.setBaseStreamingUrl(serverUrl);
		remoteResourceAccessFactory.setMarshallerRegistry(marshallerRegistry);
		remoteResourceAccessFactory.setSessionIdProvider(authContext);
		remoteResourceAccessFactory.setAuthorizationFailureListener(authContext);
		remoteResourceAccessFactory.setAuthorizationMaxRetries(2);
		remoteResourceAccessFactory.setStreamPipeFactory(StreamPipes.simpleFactory());

	}

	@After
	public void clearData() throws Exception {
		resources.clear();
	}

	// ============================= //
	// =========== TESTS =========== //
	// ============================= //

	@Test
	public void testOpenStream() throws Exception {
		testOpenStream(false, false, null, false);
	}

	@Test
	public void testOpenStreamWithMatchingCondition() throws Exception {
		testOpenStream(false, false, true, false);
	}

	@Test
	public void testOpenStreamWithUnmatchingCondition() throws Exception {
		testOpenStream(false, false, false, false);
	}

	@Test
	public void testOpenStreamWithResponseConsumer() throws Exception {
		testOpenStream(false, false, null, true);
	}

	@Test
	public void testOpenStreamWithMatchingConditionAndResponseConsumer() throws Exception {
		testOpenStream(false, false, true, true);
	}

	@Test
	public void testOpenStreamWithUnmatchingConditionAndResponseConsumer() throws Exception {
		testOpenStream(false, false, false, true);
	}

	@Test
	public void testOpenStreamSequentially() throws Exception {
		testOpenStreamSequentially(false, null);
	}

	@Test
	public void testOpenStreamSequentiallyWithMatchingCondition() throws Exception {
		testOpenStreamSequentially(false, true);
	}

	@Test
	public void testOpenStreamSequentiallyWithUnmatchingCondition() throws Exception {
		testOpenStreamSequentially(false, false);
	}

	@Test
	public void testOpenStreamConcurrently() throws Exception {
		testOpenStreamConcurrently(false, null);
	}

	@Test
	public void testOpenStreamConcurrentlyWithMatchingCondition() throws Exception {
		testOpenStreamConcurrently(false, true);
	}

	@Test
	public void testOpenStreamConcurrentlyWithUnmatchingCondition() throws Exception {
		testOpenStreamConcurrently(false, false);
	}

	@Test
	public void testOpenStreamReAuthorizing() throws Exception {
		testOpenStream(true, false, null, false);
	}

	@Test
	public void testOpenStreamReAuthorizingWithMatchingCondition() throws Exception {
		testOpenStream(true, false, true, false);
	}

	@Test
	public void testOpenStreamReAuthorizingWithUnmatchingCondition() throws Exception {
		testOpenStream(true, false, false, false);
	}

	@Test
	public void testOpenStreamReAuthorizingWithResponseConsumer() throws Exception {
		testOpenStream(true, false, null, true);
	}

	@Test
	public void testOpenStreamReAuthorizingWithMatchingConditionAndResponseConsumer() throws Exception {
		testOpenStream(true, false, true, true);
	}

	@Test
	public void testOpenStreamReAuthorizingWithUnmatchingConditionAndResponseConsumer() throws Exception {
		testOpenStream(true, false, false, true);
	}

	@Test
	public void testOpenStreamReAuthorizingSequentially() throws Exception {
		testOpenStreamSequentially(true, null);
	}

	@Test
	public void testOpenStreamReAuthorizingSequentiallyWithMatchingCondition() throws Exception {
		testOpenStreamSequentially(true, true);
	}

	@Test
	public void testOpenStreamReAuthorizingSequentiallyWithUnmatchingCondition() throws Exception {
		testOpenStreamSequentially(true, false);
	}

	@Test
	public void testOpenStreamReAuthorizingConcurrently() throws Exception {
		testOpenStreamConcurrently(true, null);
	}

	@Test
	public void testOpenStreamReAuthorizingConcurrentlyWithMatchingCondition() throws Exception {
		testOpenStreamConcurrently(true, true);
	}

	@Test
	public void testOpenStreamReAuthorizingConcurrentlyWithUnmatchingCondition() throws Exception {
		testOpenStreamConcurrently(true, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOpenStreamWithNullResource() throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		Resource resource = null;

		remoteResourceAccess.retrieve(resource).stream();

	}

	@Test(expected = UncheckedIOException.class)
	public void testOpenStreamWithNullResourceId() throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		Resource resource = Resource.T.create();
		resource.setId(null);

		remoteResourceAccess.retrieve(resource).stream();

	}

	@Test(expected = UncheckedIOException.class)
	public void testOpenStreamWithInexistentResourceId() throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		Resource resource = Resource.T.create();
		resource.setId("Inexistent ID");

		remoteResourceAccess.retrieve(resource).stream();

	}

	@Test(expected = UncheckedIOException.class)
	public void testOpenStreamForbidden() throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		TestResource testResource = createTestResource();

		authContext.forceError(403);

		remoteResourceAccess.retrieve(testResource.resource).stream();

	}

	@Test(expected = UncheckedIOException.class)
	public void testOpenStreamUnknownFailure() throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		TestResource testResource = createTestResource();

		authContext.forceError(500);

		remoteResourceAccess.retrieve(testResource.resource).stream();

	}

	@Test
	public void testWriteToStream() throws Exception {
		testWriteToStream(false, false, null, false);
	}

	@Test
	public void testWriteToStreamWithMatchingCondition() throws Exception {
		testWriteToStream(false, false, true, false);
	}

	@Test
	public void testWriteToStreamWithUnmatchingCondition() throws Exception {
		testWriteToStream(false, false, false, false);
	}

	@Test
	public void testWriteToStreamWithResponseConsumer() throws Exception {
		testWriteToStream(false, false, null, true);
	}

	@Test
	public void testWriteToStreamWithMatchingConditionAndResponseConsumer() throws Exception {
		testWriteToStream(false, false, true, true);
	}

	@Test
	public void testWriteToStreamWithUnmatchingConditionAndResponseConsumer() throws Exception {
		testWriteToStream(false, false, false, true);
	}

	@Test
	public void testWriteToStreamSequentially() throws Exception {
		testWriteToStreamSequentially(false, null);
	}

	@Test
	public void testWriteToStreamSequentiallyWithMatchingCondition() throws Exception {
		testWriteToStreamSequentially(false, true);
	}

	@Test
	public void testWriteToStreamSequentiallyWithUnmatchingCondition() throws Exception {
		testWriteToStreamSequentially(false, false);
	}

	@Test
	public void testWriteToStreamConcurrently() throws Exception {
		testWriteToStreamConcurrently(false, null);
	}

	@Test
	public void testWriteToStreamConcurrentlyWithMatchingCondition() throws Exception {
		testWriteToStreamConcurrently(false, true);
	}

	@Test
	public void testWriteToStreamConcurrentlyWithUnmatchingCondition() throws Exception {
		testWriteToStreamConcurrently(false, false);
	}

	@Test
	public void testWriteToStreamReAuthorizing() throws Exception {
		testWriteToStream(true, false, null, false);
	}

	@Test
	public void testWriteToStreamReAuthorizingWithMatchingCondition() throws Exception {
		testWriteToStream(true, false, true, false);
	}

	@Test
	public void testWriteToStreamReAuthorizingWithUnmatchingCondition() throws Exception {
		testWriteToStream(true, false, false, false);
	}

	@Test
	public void testWriteToStreamReAuthorizingWithResponseConsumer() throws Exception {
		testWriteToStream(true, false, null, true);
	}

	@Test
	public void testWriteToStreamReAuthorizingWithMatchingConditionAndResponseConsumer() throws Exception {
		testWriteToStream(true, false, true, true);
	}

	@Test
	public void testWriteToStreamReAuthorizingWithUnmatchingConditionAndResponseConsumer() throws Exception {
		testWriteToStream(true, false, false, true);
	}

	@Test
	public void testWriteToStreamReAuthorizingSequentially() throws Exception {
		testWriteToStreamSequentially(true, null);
	}

	@Test
	public void testWriteToStreamReAuthorizingSequentiallyWithMatchingCondition() throws Exception {
		testWriteToStreamSequentially(true, true);
	}

	@Test
	public void testWriteToStreamReAuthorizingSequentiallyWithUnmatchingCondition() throws Exception {
		testWriteToStreamSequentially(true, false);
	}

	@Test
	public void testWriteToStreamReAuthorizingConcurrently() throws Exception {
		testWriteToStreamConcurrently(true, null);
	}

	@Test
	public void testWriteToStreamReAuthorizingConcurrentlyWithMatchingCondition() throws Exception {
		testWriteToStreamConcurrently(true, true);
	}

	@Test
	public void testWriteToStreamReAuthorizingConcurrentlyWithUnmatchingConditio() throws Exception {
		testWriteToStreamConcurrently(true, false);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWriteToStreamWithNullResource() throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		Resource resource = null;

		remoteResourceAccess.retrieve(resource).stream(new ByteArrayOutputStream());

	}

	@Test(expected = UncheckedIOException.class)
	public void testWriteToStreamWithNullResourceId() throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		Resource resource = Resource.T.create();
		resource.setId(null);

		remoteResourceAccess.retrieve(resource).stream(new ByteArrayOutputStream());

	}

	@Test(expected = UncheckedIOException.class)
	public void testWriteToStreamWithInexistentResourceId() throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		Resource resource = Resource.T.create();
		resource.setId("Inexistent ID");

		remoteResourceAccess.retrieve(resource).stream(new ByteArrayOutputStream());

	}

	@Test(expected = UncheckedIOException.class)
	public void testWriteToStreamForbidden() throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		TestResource testResource = createTestResource();

		authContext.forceError(403);

		remoteResourceAccess.retrieve(testResource.resource).stream(new ByteArrayOutputStream());

	}

	@Test(expected = UncheckedIOException.class)
	public void testWriteToStreamUnknownFailure() throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		TestResource testResource = createTestResource();

		authContext.forceError(500);

		remoteResourceAccess.retrieve(testResource.resource).stream(new ByteArrayOutputStream());

	}

	@Test
	public void testCreateWithInputStream() throws Exception {
		testCreateWithInputStream(false, true, false);
	}

	@Test
	public void testCreateWithInputStreamSequentially() throws Exception {
		testCreateWithInputStreamSequentially(false);
	}

	@Test
	public void testCreateWithInputStreamConcurrently() throws Exception {
		testCreateWithInputStreamConcurrently(false);
	}

	@Test
	public void testCreateWithInputStreamReAuthorizing() throws Exception {
		testCreateWithInputStream(true, true, false);
	}

	@Test
	public void testCreateWithInputStreamReAuthorizingSequentially() throws Exception {
		testCreateWithInputStreamSequentially(true);
	}

	@Test
	public void testCreateWithInputStreamReAuthorizingConcurrently() throws Exception {
		testCreateWithInputStreamConcurrently(true);
	}

	@Test
	public void testCreateWithInputStreamAndNullResourceName() throws Exception {
		testCreateWithInputStream(false, false, false);
	}

	@Test
	public void testDelete() throws Exception {
		testDelete(false, false);
	}

	@Test
	public void testDeleteSequentially() throws Exception {
		testDeleteSequentially(false);

	}

	@Test
	public void testDeleteConcurrently() throws Exception {
		testDeleteConcurrently(false);
	}

	@Test
	public void testDeleteReAuthorizing() throws Exception {
		testDelete(true, false);
	}

	@Test
	public void testDeleteReAuthorizingSequentially() throws Exception {
		testDeleteSequentially(true);
	}

	@Test
	public void testDeleteReAuthorizingConcurrently() throws Exception {
		testDeleteConcurrently(true);
	}

	// ============================= //
	// ========= COMMONS =========== //
	// ============================= //

	protected void testOpenStream(boolean reAuthorizing, boolean multiThreaded, Boolean matchingCondition, boolean consumeResponse) throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		TestResource testResource = createTestResource();

		if (reAuthorizing) {
			authContext.invalidate();
		}

		ResourceRetrieveBuilder builder = remoteResourceAccess.retrieve(testResource.resource);

		addCondition(matchingCondition, testResource, builder);

		ResponseHolder consumer = null;

		if (consumeResponse) {
			consumer = new ResponseHolder();
			builder.onResponse(consumer);
		}

		InputStream is = builder.stream();

		if (consumer != null) {
			assertResponse(matchingCondition, testResource, consumer);
		}

		if (TRUE.equals(matchingCondition)) {
			Assert.assertNull("InputStream should have been null when a matching condition is given", is);
		} else {
			Assert.assertArrayEquals(testResource.resourceData, IOTools.slurpBytes(is, true));
		}

		if (!TRUE.equals(matchingCondition) && reAuthorizing && !multiThreaded) {
			Assert.assertNotNull(authContext.getNotifiedFailures());
			Assert.assertFalse(authContext.getNotifiedFailures().isEmpty());
		}

	}

	protected void testOpenStreamSequentially(final boolean reAuthorizing, final Boolean matchingCondition) throws Exception {
		for (int i = 0; i < MAX_SEQUENTIAL_TESTS; i++) {
			testOpenStream(reAuthorizing, false, matchingCondition, false);
		}
	}

	protected void testOpenStreamConcurrently(final boolean reAuthorizing, final Boolean matchingCondition) throws Exception {
		testConcurrently(new TestCaller() {
			@Override
			public void test() throws Throwable {
				testOpenStream(reAuthorizing, true, matchingCondition, false);
			}
		}, MAX_CONCURRENT_TESTS);
	}

	protected void testWriteToStream(boolean reAuthorizing, boolean multiThreaded, Boolean matchingCondition, boolean consumeResponse)
			throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		TestResource testResource = createTestResource();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		if (reAuthorizing) {
			authContext.invalidate();
		}

		ResourceRetrieveBuilder builder = remoteResourceAccess.retrieve(testResource.resource);

		addCondition(matchingCondition, testResource, builder);

		ResponseHolder consumer = null;

		if (consumeResponse) {
			consumer = new ResponseHolder();
			builder.onResponse(consumer);
		}

		builder.stream(outputStream);

		if (consumer != null) {
			assertResponse(matchingCondition, testResource, consumer);
		}

		if (TRUE.equals(matchingCondition)) {
			Assert.assertEquals("No data should have been written as a matching condition was given", 0, outputStream.toByteArray().length);
		} else {
			Assert.assertArrayEquals(testResource.resourceData, outputStream.toByteArray());
		}

		if (!TRUE.equals(matchingCondition) && reAuthorizing && !multiThreaded) {
			Assert.assertNotNull(authContext.getNotifiedFailures());
			Assert.assertFalse(authContext.getNotifiedFailures().isEmpty());
		}

	}

	protected void testWriteToStreamSequentially(final boolean reAuthorizing, final Boolean matchingCondition) throws Exception {
		for (int i = 0; i < MAX_SEQUENTIAL_TESTS; i++) {
			testWriteToStream(reAuthorizing, false, matchingCondition, false);
		}
	}

	protected void testWriteToStreamConcurrently(final boolean reAuthorizing, final Boolean matchingCondition) throws Exception {
		testConcurrently(new TestCaller() {
			@Override
			public void test() throws Throwable {
				testWriteToStream(reAuthorizing, true, matchingCondition, false);
			}
		}, MAX_CONCURRENT_TESTS);
	}

	protected void testDelete(boolean reAuthorizing, boolean multiThreaded) throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		Resource resource = remoteResourceAccess.create().name("delete-test-" + UUID.randomUUID()).store(randomStream());

		// Before deletion assertions
		TestResource createdResource = resources.get(resource.getId());
		Assert.assertNotNull(createdResource);
		Assert.assertNotNull(createdResource.resourceData);

		if (reAuthorizing) {
			authContext.invalidate();
		}

		remoteResourceAccess.delete(resource).delete();

		if (reAuthorizing && !multiThreaded) {
			Assert.assertNotNull(authContext.getNotifiedFailures());
			Assert.assertFalse(authContext.getNotifiedFailures().isEmpty());
		}

		// After deletion assertions
		TestResource deletedResource = resources.get(resource.getId());
		Assert.assertNull(deletedResource);

	}

	protected void testDeleteSequentially(final boolean reAuthorizing) throws Exception {
		for (int i = 0; i < MAX_SEQUENTIAL_TESTS; i++) {
			testDelete(reAuthorizing, false);
		}
	}

	protected void testDeleteConcurrently(final boolean reAuthorizing) throws Exception {
		testConcurrently(new TestCaller() {
			@Override
			public void test() throws Throwable {
				testDelete(reAuthorizing, true);
			}
		}, MAX_CONCURRENT_TESTS);
	}

	protected void testCreateWithInputStream(boolean reAuthorizing, boolean provideResourceName, boolean multiThreaded) throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		TestResource testResource = createTestResource();

		InputStream in = new ByteArrayInputStream(testResource.resourceData);

		if (reAuthorizing) {
			authContext.invalidate();
		}

		String resourceName = provideResourceName ? "copy-of-" + testResource.resource.getName() : null;

		Resource resource = remoteResourceAccess.create().name(resourceName).store(in);
		// Resource resource = remoteResourceAccess.create(in, provideResourceName ? "copy-of-" +
		// testResource.resource.getName() : null);

		Assert.assertNotEquals(testResource.resource.getId(), resource.getId());
		Assert.assertNotEquals(testResource.resource.getName(), resource.getName());
		Assert.assertEquals(testResource.resource.getMd5(), resource.getMd5());

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		// remoteResourceAccess.writeToStream(resource, outputStream);
		remoteResourceAccess.retrieve(resource).stream(outputStream);

		byte[] bytes = outputStream.toByteArray();
		Assert.assertArrayEquals(testResource.resourceData, bytes);

		if (reAuthorizing && !multiThreaded) {
			Assert.assertNotNull(authContext.getNotifiedFailures());
			Assert.assertFalse(authContext.getNotifiedFailures().isEmpty());
		}

	}

	protected void testCreateWithInputStreamSequentially(final boolean reAuthorizing) throws Exception {
		for (int i = 0; i < MAX_SEQUENTIAL_TESTS; i++) {
			testCreateWithInputStream(reAuthorizing, true, false);
		}
	}

	protected void testCreateWithInputStreamConcurrently(final boolean reAuthorizing) throws Exception {
		testConcurrently(new TestCaller() {
			@Override
			public void test() throws Throwable {
				testCreateWithInputStream(reAuthorizing, true, true);
			}
		}, MAX_CONCURRENT_TESTS);
	}

	protected void testConcurrently(TestCaller test, int numThreads) throws Exception {

		List<TestCaller> callers = new ArrayList<>();

		for (int i = 0; i < numThreads; i++) {
			callers.add(test);
		}

		testConcurrently(callers);

	}

	protected void testConcurrently(List<TestCaller> tests) throws Exception {

		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE, new NamedPoolThreadFactory(getClass().getSimpleName()));

		try {
			List<Future<Throwable>> results = executorService.invokeAll(tests, CONCURRENT_TESTS_TIMEOUT, CONCURRENT_TESTS_TIMEOUT_UNIT);

			List<Throwable> errors = new ArrayList<>();

			for (Future<Throwable> result : results) {
				Throwable error = null;

				try {
					error = result.get();
				} catch (CancellationException e) {
					System.out.println("Test cancelled as it didn't complete after " + CONCURRENT_TESTS_TIMEOUT + " "
							+ CONCURRENT_TESTS_TIMEOUT_UNIT.toString().toLowerCase());
					continue;
				}

				if (error != null) {
					errors.add(error);
				}
			}

			if (errors.isEmpty()) {
				System.out.println(tests.size() + " concurrent tests completed successfully.");
			} else {
				AssertionError error = new AssertionError("From " + tests.size() + " concurrent tests, " + errors.size() + " failed.");
				int i = 1;
				for (Throwable cause : errors) {
					log.error("Failed concurrent test #" + (i++) + ": " + cause, cause);
					error.addSuppressed(cause);
				}
				throw error;
			}
		} finally {
			executorService.shutdownNow();
		}

	}

	private void assertResponse(Boolean matchingCondition, TestResource testResource, ResponseHolder consumer) {
		BinaryRetrievalResponse response = consumer.get();
		Assert.assertNotNull(response);
		Assert.assertNotNull(response.getCacheControl());
		Assert.assertEquals(testResource.resource.getMd5(), response.getCacheControl().getFingerprint());
		if (response instanceof StreamBinaryResponse) {
			StreamBinaryResponse streamResponse = (StreamBinaryResponse) response;
			if (TRUE.equals(matchingCondition)) {
				Assert.assertTrue(streamResponse.getNotStreamed());
			} else {
				Assert.assertFalse(streamResponse.getNotStreamed());
			}
		}
	}

	private void addCondition(Boolean matchingCondition, TestResource testResource, ResourceRetrieveBuilder builder) {

		if (TRUE.equals(matchingCondition)) {

			FingerprintMismatch condition = FingerprintMismatch.T.create();
			condition.setFingerprint(testResource.resource.getMd5());
			builder.condition(condition);

		} else if (FALSE.equals(matchingCondition)) {

			FingerprintMismatch condition = FingerprintMismatch.T.create();
			condition.setFingerprint(UUID.randomUUID().toString());
			builder.condition(condition);

		}

	}

	private class ResponseHolder implements Consumer<BinaryRetrievalResponse>, Supplier<BinaryRetrievalResponse> {

		private BinaryRetrievalResponse response;

		@Override
		public BinaryRetrievalResponse get() {
			return response;
		}

		@Override
		public void accept(BinaryRetrievalResponse response) {
			this.response = response;
		}

	}

	public abstract class TestCaller implements Callable<Throwable> {

		public abstract void test() throws Throwable;

		@Override
		public Throwable call() throws Exception {
			try {
				test();
				return null;
			} catch (Throwable e) {
				return e;
			}
		}

	}

	/**
	 * <p>
	 * A {@link ThreadFactory} allowing a custom name for the generated Thread(s).
	 */
	static class NamedPoolThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger(1);
		private final AtomicInteger threadNumber = new AtomicInteger(1);
		private final String namePrefix;

		NamedPoolThreadFactory(String poolName) {
			namePrefix = poolName + "-pool-" + poolNumber.getAndIncrement() + "-thread-";
		}

		@Override
		public Thread newThread(Runnable r) {
			Thread t = Thread.ofVirtual().name(namePrefix + threadNumber.getAndIncrement()).unstarted(r);
			if (t.getPriority() != Thread.NORM_PRIORITY) {
				t.setPriority(Thread.NORM_PRIORITY);
			}
			return t;
		}

	}

	private static MarshallerRegistry createMarshallerRegistry(final Marshaller marshaller, final String type) {

		return new MarshallerRegistry() {

			@Override
			public Marshaller getMarshaller(String mimeType) {
				return marshaller;
			}

			@Override
			public MarshallerRegistryEntry getMarshallerRegistryEntry(String mimeType) {
				return new MarshallerRegistryEntry() {

					@Override
					public String getMimeType() {
						return type;
					}

					@Override
					public Marshaller getMarshaller() {
						return marshaller;
					}

				};
			}
		};

	}

	protected static TestResource createTestResource(InputStream in, String givenName) throws IOException {
		try {
			return createTestResource(IOTools.slurpBytes(in), givenName);
		} finally {
			IOTools.closeCloseable(in, null);
		}
	}

	protected static TestResource createTestResource(byte[] data, String givenName) {
		String resourceId = UUID.randomUUID().toString();

		Resource resource = Resource.T.create();
		resource.setId(resourceId);
		resource.setName(givenName != null ? givenName : "test-resource-" + resourceId);
		resource.setMd5(hash(data));

		TestResource randomData = new TestResource();
		randomData.resource = resource;
		randomData.resourceData = data;

		resources.put(resourceId, randomData);

		return randomData;
	}

	protected static TestResource createTestResource() {
		return createTestResource(randomData(), null);
	}

	protected static byte[] randomData() {
		byte[] data = new byte[TEST_DATA_SIZE];
		new Random().nextBytes(data);
		return data;
	}

	protected InputStream randomStream() {
		return new ByteArrayInputStream(randomData());
	}

	protected static class TestResource {
		public Resource resource;
		public byte[] resourceData;
	}

	protected static String hash(byte[] data) {
		try {
			MessageDigest md;
			md = MessageDigest.getInstance("MD5");
			byte[] md5hash = null;
			md.update(data, 0, data.length);
			md5hash = md.digest();
			return convertToHex(md5hash);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	protected static String convertToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			int halfbyte = (data[i] >>> 4) & 0x0F;
			int two_halfs = 0;
			do {
				if ((0 <= halfbyte) && (halfbyte <= 9))
					buf.append((char) ('0' + halfbyte));
				else
					buf.append((char) ('a' + (halfbyte - 10)));
				halfbyte = data[i] & 0x0F;
			} while (two_halfs++ < 1);
		}
		return buf.toString();
	}

	protected static class TestHandler implements HttpHandler {

		@Override
		public void handleRequest(final HttpServerExchange exchange) throws Exception {
			if (exchange.isInIoThread()) {
				exchange.dispatch(this);
			} else {
				handleTestRequest(exchange);
			}
		}

		protected void handleTestRequest(final HttpServerExchange exchange) throws Exception {

			HttpString method = exchange.getRequestMethod();

			if (method == null) {
				throw new RuntimeException("Cannot test unknown http method");
			}

			try {
				String sessionId = getQueryParameter(exchange, "sessionId");

				Integer errorToForce = null;
				if (sessionId.equals(TestAuthorizationContext.invalidSessionId)) {
					errorToForce = 401;
				} else {
					errorToForce = Integer.parseInt(sessionId);
				}

				respondWithFailure(exchange, errorToForce);

			} catch (Exception e) {
				if ("GET".equals(method.toString())) {
					handleTestGetRequest(exchange);
				} else if ("POST".equals(method.toString())) {
					handleTestPostRequest(exchange);
				} else {
					throw new RuntimeException("Unexpected http method: " + method);
				}
			}

		}

		protected void handleTestGetRequest(final HttpServerExchange exchange) throws Exception {

			String resourceId = getQueryParameter(exchange, "resourceId");

			TestResource testResource = resources.get(resourceId);

			int httpResponse = 200;
			if (testResource == null) {
				httpResponse = 404;
			}

			if (httpResponse >= 300) {
				respondWithFailure(exchange, httpResponse);
			} else {
				exchange.setStatusCode(httpResponse);
				exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
				exchange.getResponseSender().send(ByteBuffer.wrap(testResource.resourceData));
			}

		}

		protected void handleTestPostRequest(final HttpServerExchange exchange) throws Exception {

			FormDataParser parser = new MultiPartParserDefinition().create(exchange);
			TestResource testResource = null;

			exchange.startBlocking();

			if (parser != null) {

				FormData formData = parser.parseBlocking();

				Deque<FormValue> deque = formData.get("content");

				FormValue formValue = deque.poll();

				if (formValue.isFile()) {

					File file = formValue.getPath().toFile();
					String fileName = formValue.getFileName();

					ByteArrayOutputStream out = new ByteArrayOutputStream();
					IOTools.pump(Files.newInputStream(file.toPath()), out);

					testResource = createTestResource(out.toByteArray(), fileName);

				} else {
					throw new RuntimeException("Part [ content ] not intepreted as a file");
				}

			} else {

				ByteArrayOutputStream out = new ByteArrayOutputStream();

				InputStream in = exchange.getInputStream();
				IOTools.pump(in, out);
				in.close();

				String fileName = getQueryParameter(exchange, "fileName");
				testResource = createTestResource(out.toByteArray(), fileName);

			}

			List<Resource> resourceList = new ArrayList<>();
			resourceList.add(testResource.resource);

			ByteArrayOutputStream assemblyOut = new ByteArrayOutputStream();
			marshaller.marshall(assemblyOut, resourceList);
			exchange.getResponseSender().send(ByteBuffer.wrap(assemblyOut.toByteArray()));

		}

		protected void respondWithFailure(final HttpServerExchange exchange, final Integer expectedErrorCode) throws Exception {
			exchange.setStatusCode(expectedErrorCode);
			exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
			exchange.getResponseSender().send("Request failed: " + expectedErrorCode);
		}

		protected String getQueryParameter(HttpServerExchange exchange, String name) {

			Deque<String> deque = exchange.getQueryParameters().get(name);

			if (deque != null) {
				String value = deque.pop();
				return value;
			}

			return null;

		}

	}

	protected static class TestServlet extends HttpServlet {

		private static final long serialVersionUID = 1L;

		@Override
		protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

			String resourceId = request.getParameter("resourceId");

			String etag = null;

			TestResource testResource = resources.get(resourceId);

			int httpResponse = responseCodeFor(request);
			if (testResource == null) {
				try {
					httpResponse = Integer.valueOf(resourceId);
				} catch (Exception e) {
					httpResponse = 404;
				}
			} else {
				etag = testResource.resource.getMd5();
			}

			String ifNoneMatch = request.getHeader(Headers.IF_NONE_MATCH_STRING);

			if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
				httpResponse = 304;
			}

			response.setStatus(httpResponse);
			response.setHeader(Headers.CONTENT_TYPE_STRING, "text/plain");
			if (etag != null)
				response.setHeader(Headers.ETAG_STRING, etag);

			if (httpResponse >= 300) {
				writeResponse(response, httpResponse);
			} else {
				writeResponse(response, testResource.resourceData);
			}

		}

		@Override
		protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

			String resourceId = request.getParameter("resourceId");

			TestResource testResource = resources.get(resourceId);

			int httpResponse = responseCodeFor(request);
			if (testResource == null) {
				try {
					httpResponse = Integer.valueOf(resourceId);
				} catch (Exception e) {
					httpResponse = 404;
				}
			}

			if (httpResponse < 300) {
				// Normally the binary data only would be deleted, but here we purge the entry to avoid affecting other tests.
				resources.remove(resourceId);
			}

			response.setStatus(httpResponse);
			response.setHeader(Headers.CONTENT_TYPE.toString(), "text/plain");

		}

		@Override
		protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

			int httpResponse = responseCodeFor(request);

			if (httpResponse < 300) {

				TestResource testResource = null;

				if (isMultipart(request)) {

					Collection<Part> parts = request.getParts();

					for (Part part : parts) {
						if (part != null && "content".equals(part.getName())) {
							testResource = createTestResource(part.getInputStream(), getFileName(part));
							break;
						}
					}

				} else {

					testResource = createTestResource(request.getInputStream(), request.getParameter("fileName"));

				}

				List<Resource> resourceList = new ArrayList<>();
				resourceList.add(testResource.resource);
				OutputStream out = response.getOutputStream();

				try {
					marshaller.marshall(out, resourceList);
				} catch (Exception e) {
					throw new IOException(e);
				} finally {
					IOTools.closeQuietly(out);
				}

			} else {
				writeResponse(response, httpResponse);
			}

		}

		protected void writeResponse(HttpServletResponse response, byte[] data) throws IOException {
			OutputStream out = response.getOutputStream();
			try {
				IOTools.pump(new ByteArrayInputStream(data), out);
				out.flush();
			} finally {
				IOTools.closeQuietly(out);
			}
		}

		protected void writeResponse(HttpServletResponse response, int errorCode) throws IOException {
			response.setStatus(errorCode);
			PrintWriter writer = response.getWriter();
			writer.println("Request failed: " + errorCode);
			writer.flush();
			writer.close();
		}

		private static boolean isMultipart(HttpServletRequest request) {

			if (request.getContentType() == null)
				return false;

			return (request.getContentType().toLowerCase().startsWith("multipart"));
		}

		private static String getFileName(Part part) throws ServletException {
			String contentDisp = part.getHeader("content-disposition");
			if (contentDisp == null)
				throw new ServletException("part is missing content-disposition header");
			for (String cd : contentDisp.split(";")) {
				if (cd.trim().startsWith("filename")) {
					return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
				}
			}
			return null;
		}

		private static Integer responseCodeFor(HttpServletRequest request) {

			String sessionId = request.getParameter("sessionId");

			Integer errorToForce = null;
			if (sessionId.equals(TestAuthorizationContext.invalidSessionId)) {
				errorToForce = 401;
			} else {
				try {
					errorToForce = Integer.parseInt(sessionId);
				} catch (Exception e) {
					errorToForce = 200;
				}
			}

			return errorToForce;

		}

	}

	protected static class TestAuthorizationContext implements Supplier<String>, Consumer<Throwable> {

		public ThreadLocal<List<Throwable>> notifiedExceptions = new ThreadLocal<>();
		public static final String invalidSessionId = "invalid";
		public static final String validSessionId = "valid";

		public String sessionId = validSessionId;

		@Override
		public void accept(Throwable object) throws RuntimeException {

			if (notifiedExceptions.get() == null) {
				notifiedExceptions.set(new ArrayList<Throwable>());
			}

			notifiedExceptions.get().add(object);

			sessionId = validSessionId;

		}

		@Override
		public String get() throws RuntimeException {
			return sessionId;
		}

		public void invalidate() {
			this.sessionId = invalidSessionId;
		}

		public void forceError(Integer errorCode) {
			this.sessionId = errorCode.toString();
		}

		public List<Throwable> getNotifiedFailures() {
			return notifiedExceptions.get();
		}

	}

}
