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
package com.braintribe.model.processing.resource.server.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.processing.resource.server.WebStreamingServer;
import com.braintribe.model.processing.resource.server.test.commons.TestAuthorizationContext;
import com.braintribe.model.processing.resource.server.test.commons.TestResourceData;
import com.braintribe.model.processing.resource.server.test.commons.TestResourceDataTools;
import com.braintribe.model.processing.resource.streaming.access.RemoteResourceAccess;
import com.braintribe.model.processing.resource.streaming.access.RemoteResourceAccessFactory;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.resource.Resource;
import com.braintribe.utils.IOTools;

/**
 * {@link WebStreamingServer} test suite based on {@link RemoteResourceAccess}.
 * 
 * <p>
 * This suite manages an instance of Undertow embedded web server.
 * 
 */
public class RemoteResourceAccessBasedTest extends GmWebStreamingServerTestBase {

	private RemoteResourceAccessFactory remoteResourceAccessFactory;
	private TestAuthorizationContext authContext;

	// ============================= //
	// ======== LIFE CYCLE ========= //
	// ============================= //

	@BeforeClass
	public static void beforeClass() throws Exception {

		GmWebStreamingServerTestBase.initialize();

		servletUrl = startServer();

		createTestResource();

	}

	@AfterClass
	public static void afterClass() throws Exception {
		shutdownServer();
		destroy();
	}

	@Before
	public void setupRemoteResourceAccessFactory() throws Exception {

		authContext = context.contract().userSessionIdProvider();
		authContext.reset();

		remoteResourceAccessFactory = context.contract().remoteResourceAccessFactory();
		remoteResourceAccessFactory.setBaseStreamingUrl(servletUrl);
		remoteResourceAccessFactory.setAuthorizationFailureListener(authContext);
		remoteResourceAccessFactory.setAuthorizationMaxRetries(5);

	}

	// ============================= //
	// =========== TESTS =========== //
	// ============================= //

	@Test
	public void testOpenStream() throws Exception {
		testOpenStream(false, false);
	}

	@Test
	public void testOpenStreamSequentially() throws Exception {
		testOpenStreamSequentially(false);
	}

	@Test
	public void testOpenStreamConcurrently() throws Exception {
		testOpenStreamConcurrently(false);
	}

	@Test
	public void testOpenStreamReAuthorizing() throws Exception {
		testOpenStream(true, false);
	}

	@Test
	public void testOpenStreamReAuthorizingSequentially() throws Exception {
		testOpenStreamSequentially(true);
	}

	@Test
	public void testOpenStreamReAuthorizingConcurrently() throws Exception {
		testOpenStreamConcurrently(true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testOpenStreamWithNullResource() throws Exception {
		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		Resource resource = null;

		remoteResourceAccess.openStream(resource);
	}

	@Test(expected = IOException.class)
	public void testOpenStreamWithNullResourceId() throws Exception {
		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		Resource resource = Resource.T.create();

		resource.setId(null);

		remoteResourceAccess.openStream(resource);
	}

	@Test(expected = IOException.class)
	public void testOpenStreamWithInexistentResourceId() throws Exception {
		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		Resource resource = Resource.T.create();

		resource.setId("Inexistent ID");

		remoteResourceAccess.openStream(resource);
	}

	@Test(expected = IOException.class)
	public void testOpenStreamUnauthorized() throws Exception {

		remoteResourceAccessFactory.setAuthorizationFailureListener(null);

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		authContext.invalidate();

		remoteResourceAccess.openStream(testResource);

	}

	@Test
	public void testWriteToStream() throws Exception {
		testWriteToStream(false, false);
	}

	@Test
	public void testWriteToStreamSequentially() throws Exception {
		testWriteToStreamSequentially(false);
	}

	@Test
	public void testWriteToStreamConcurrently() throws Exception {
		testWriteToStreamConcurrently(false);
	}

	@Test
	public void testWriteToStreamReAuthorizing() throws Exception {
		testWriteToStream(true, false);
	}

	@Test
	public void testWriteToStreamReAuthorizingSequentially() throws Exception {
		testWriteToStreamSequentially(true);
	}

	@Test
	public void testWriteToStreamReAuthorizingConcurrently() throws Exception {
		testWriteToStreamConcurrently(true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testWriteToStreamWithNullResource() throws Exception {
		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		Resource resource = null;

		remoteResourceAccess.writeToStream(resource, new ByteArrayOutputStream());
	}

	@Test(expected = IOException.class)
	public void testWriteToStreamWithNullResourceId() throws Exception {
		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		Resource resource = Resource.T.create();
		resource.setId(null);

		remoteResourceAccess.writeToStream(resource, new ByteArrayOutputStream());
	}

	@Test(expected = IOException.class)
	public void testWriteToStreamWithInexistentResourceId() throws Exception {
		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		Resource resource = Resource.T.create();
		resource.setId("Inexistent ID");

		remoteResourceAccess.writeToStream(resource, new ByteArrayOutputStream());
	}

	@Test(expected = IOException.class)
	public void testWriteToStreamUnauthorized() throws Exception {
		remoteResourceAccessFactory.setAuthorizationFailureListener(null);

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		authContext.invalidate();

		remoteResourceAccess.writeToStream(testResource, new ByteArrayOutputStream());
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

	@Test(expected = NullPointerException.class)
	public void testCreateWithNullInputStream() throws Exception {
		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		InputStream in = null;

		remoteResourceAccess.create().name("failure-test").store(in);
	}

	@Test(expected = UncheckedIOException.class)
	public void testCreateWithNullInputStreamUnauthorized() throws Exception {
		remoteResourceAccessFactory.setAuthorizationFailureListener(null);

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		authContext.invalidate();

		remoteResourceAccess.create().name("failure-test").store(new ByteArrayInputStream(new byte[0]));
	}

	@Test
	public void testCreateWithInputStreamAndNullResourceName() throws Exception {
		testCreateWithInputStream(false, false, false);
	}

	// ============================= //
	// ========= COMMONS =========== //
	// ============================= //

	protected void testOpenStream(boolean reAuthorizing, boolean multiThreaded) throws Exception {
		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		if (reAuthorizing) {
			authContext.invalidate();
		}

		InputStream is = remoteResourceAccess.openStream(testResource);

		Assert.assertArrayEquals(testResourceData.data, IOTools.slurpBytes(is));

		if (reAuthorizing && !multiThreaded) {
			Assert.assertNotNull(authContext.getNotifiedFailures());
			Assert.assertFalse(authContext.getNotifiedFailures().isEmpty());
		}
	}

	protected void testOpenStreamSequentially(boolean reAuthorizing) throws Exception {
		for (int i = 0; i < MAX_SEQUENTIAL_TESTS; i++) {
			testOpenStream(reAuthorizing, false);
		}
	}

	protected void testOpenStreamConcurrently(final boolean reAuthorizing) throws Exception {
		testConcurrently(new TestCaller() {
			@Override
			public void test() throws Throwable {
				testOpenStream(reAuthorizing, true);
			}
		}, MAX_CONCURRENT_TESTS);
	}

	protected void testWriteToStream(boolean reAuthorizing, boolean multiThreaded) throws Exception {
		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		if (reAuthorizing) {
			authContext.invalidate();
		}

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		remoteResourceAccess.writeToStream(testResource, outputStream);

		Assert.assertArrayEquals(testResourceData.data, outputStream.toByteArray());

		if (reAuthorizing && !multiThreaded) {
			Assert.assertNotNull(authContext.getNotifiedFailures());
			Assert.assertFalse(authContext.getNotifiedFailures().isEmpty());
		}
	}

	protected void testWriteToStreamSequentially(final boolean reAuthorizing) throws Exception {
		for (int i = 0; i < MAX_SEQUENTIAL_TESTS; i++) {
			testWriteToStream(reAuthorizing, false);
		}
	}

	protected void testWriteToStreamConcurrently(final boolean reAuthorizing) throws Exception {
		testConcurrently(new TestCaller() {
			@Override
			public void test() throws Throwable {
				testWriteToStream(reAuthorizing, true);
			}
		}, MAX_CONCURRENT_TESTS);
	}

	protected void testCreateWithInputStream(boolean reAuthorizing, boolean multiThreaded, boolean provideResourceName) throws Exception {

		ResourceAccess remoteResourceAccess = remoteResourceAccessFactory.newInstance(null);

		TestResourceData testData = TestResourceDataTools.createResourceData();

		if (reAuthorizing) {
			authContext.invalidate();
		}

		Resource resource = remoteResourceAccess.create().name(provideResourceName ? "test-resource.bin" : null).store(testData.openInputStream());

		Assert.assertEquals(testData.md5, resource.getMd5());

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		remoteResourceAccess.writeToStream(resource, outputStream);

		byte[] bytes = outputStream.toByteArray();
		Assert.assertArrayEquals(testData.data, bytes);

		if (reAuthorizing && !multiThreaded) {
			Assert.assertNotNull(authContext.getNotifiedFailures());
			Assert.assertFalse(authContext.getNotifiedFailures().isEmpty());
		}

	}

	protected void testCreateWithInputStreamSequentially(final boolean reAuthorizing) throws Exception {
		for (int i = 0; i < MAX_SEQUENTIAL_TESTS; i++) {
			testCreateWithInputStream(reAuthorizing, false, true);
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

}
