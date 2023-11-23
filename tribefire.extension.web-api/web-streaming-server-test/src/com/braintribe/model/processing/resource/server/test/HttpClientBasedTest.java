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

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.util.EntityUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.codec.marshaller.api.Marshaller;
import com.braintribe.codec.marshaller.api.MarshallerRegistry;
import com.braintribe.model.processing.resource.server.WebStreamingServer;
import com.braintribe.model.processing.resource.server.test.commons.TestResourceData;
import com.braintribe.model.processing.resource.server.test.commons.TestResourceDataTools;
import com.braintribe.model.resource.Resource;
import com.braintribe.transport.http.DefaultHttpClientProvider;
import com.braintribe.transport.http.HttpClientProvider;
import com.braintribe.utils.IOTools;
import com.braintribe.utils.UrlTools;

/**
 * {@link WebStreamingServer } test suite based on {@link HttpClient}.
 * 
 * <p>
 * This suite manages an instance of Undertow embedded web server.
 * 
 */
public class HttpClientBasedTest extends GmWebStreamingServerTestBase {

	private static final String MIME_TYPE_BIN = "application/gm";
	private static final String MIME_TYPE_XML = "application/xml";
	private static final String MIME_TYPE_JSON = "application/json";

	private static MarshallerRegistry marshallerRegistry;
	private static Supplier<String> userSessionIdProvider;
	private static HttpClientProvider httpClientProvider = new DefaultHttpClientProvider();
	private static HttpClient cachedClient;

	// ============================= //
	// ======== LIFE CYCLE ========= //
	// ============================= //

	@BeforeClass
	public static void beforeClass() throws Exception {

		GmWebStreamingServerTestBase.initialize();

		servletUrl = startServer();

		createTestResource();

		marshallerRegistry = context.contract().marshallerRegistry();
		userSessionIdProvider = context.contract().userSessionIdProvider();

	}

	@AfterClass
	public static void afterClass() throws Exception {
		shutdownServer();
		destroy();
	}

	// ==================================== //
	// =========== DOWNLOAD TESTS ========= //
	// ==================================== //

	@Test
	public void testDownload() throws Exception {
		testDownload(testResource.getId(), "", true, true, null, 200, testResourceData);
	}

	@Test
	public void testNotFoundDownload() throws Exception {
		testDownload("INEXISTENT-ID", "", true, true, null, 404, null);
	}

	@Test
	public void testDownloadAlternativeFileName() throws Exception {
		testDownload(testResource.getId(), "", true, true, "downloded-" + testResourceData.name, 200, testResourceData);
	}

	@Test
	public void testUnauthorizedDownload() throws Exception {
		testDownload(testResource.getId(), "", false, true, null, 401, testResourceData);
	}

	// ==================================== //
	// =========== DOWNLOAD TESTS ========= //
	// ==================================== //

	@Test
	public void testDownloadRanged() throws Exception {
		testDownload(testResource.getId(), "", true, true, null, 200, testResourceData, 0L, 10L);
	}

	// ================================================= //
	// =========== DOWNLOAD TESTS - SEQUENCE =========== //
	// ================================================= //

	@Test
	public void testDownloadSequentially() throws Exception {
		testDownloadSequentially(testResource.getId(), "", true, true, null, 200, testResourceData);
	}

	@Test
	public void testNotFoundDownloadSequentially() throws Exception {
		testDownloadSequentially("INEXISTENT-ID", "", true, true, null, 404, null);
	}

	@Test
	public void testDownloadAlternativeFileNameSequentially() throws Exception {
		testDownloadSequentially(testResource.getId(), "", true, true, "downloded-" + testResourceData.name, 200, testResourceData);
	}

	@Test
	public void testUnauthorizedDownloadSequentially() throws Exception {
		testDownloadSequentially(testResource.getId(), "", false, true, null, 401, testResourceData);
	}

	// ================================================= //
	// =========== DOWNLOAD TESTS - CONCURRENT ========= //
	// ================================================= //

	@Test
	public void testDownloadConcurrently() throws Exception {
		testDownloadConcurrently(testResource.getId(), "", true, true, null, 200, testResourceData);
	}

	@Test
	public void testNotFoundDownloadConcurrently() throws Exception {
		testDownloadConcurrently("INEXISTENT-ID", "", true, true, null, 404, null);
	}

	@Test
	public void testDownloadAlternativeFileNameConcurrently() throws Exception {
		testDownloadConcurrently(testResource.getId(), "", true, true, "downloded-" + testResourceData.name, 200, testResourceData);
	}

	@Test
	public void testUnauthorizedDownloadConcurrently() throws Exception {
		testDownloadConcurrently(testResource.getId(), "", false, true, null, 401, testResourceData);
	}

	// ==================================== //
	// =========== UPLOAD TESTS =========== //
	// ==================================== //

	@Test
	public void testNonMultipartUpload() throws Exception {
		testUpload("", true, null, false, 200, createTestData(1));
	}

	@Test
	public void testUnauthorizedNonMultipartUpload() throws Exception {
		testUpload("", false, null, false, 401, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithXmlResponse() throws Exception {
		testUpload("", true, MIME_TYPE_XML, false, 200, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithBinResponse() throws Exception {
		testUpload("", true, MIME_TYPE_BIN, false, 200, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithJsonResponse() throws Exception {
		testUpload("", true, MIME_TYPE_JSON, false, 200, createTestData(1));
	}

	@Test
	public void testMultipartUpload() throws Exception {
		testUpload("", true, null, true, 200, createTestData(1));
	}

	@Test
	public void testUnauthorizedMultipartUpload() throws Exception {
		testUpload("", false, null, true, 401, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithXmlResponse() throws Exception {
		testUpload("", true, MIME_TYPE_XML, true, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithBinResponse() throws Exception {
		testUpload("", true, MIME_TYPE_BIN, true, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithJsonResponse() throws Exception {
		testUpload("", true, MIME_TYPE_JSON, true, 200, createTestData(1));
	}

	@Test
	public void testBulkMultipartUpload() throws Exception {
		testUpload("", true, null, true, 200, createTestData(5));
	}

	@Test
	public void testUnauthorizedBulkMultipartUpload() throws Exception {
		testUpload("", false, null, true, 401, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithXmlResponse() throws Exception {
		testUpload("", true, MIME_TYPE_XML, true, 200, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithBinResponse() throws Exception {
		testUpload("", true, MIME_TYPE_BIN, true, 200, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithJsonResponse() throws Exception {
		testUpload("", true, MIME_TYPE_JSON, true, 200, createTestData(5));
	}

	// ================================================= //
	// =========== UPLOAD TESTS - SEQUENTIAL =========== //
	// ================================================= //

	@Test
	public void testNonMultipartUploadSequentially() throws Exception {
		testUploadSequentially("", true, null, false, 200, createTestData(1));
	}

	@Test
	public void testUnauthorizedNonMultipartUploadSequentially() throws Exception {
		testUploadSequentially("", false, null, false, 401, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithXmlResponseSequentially() throws Exception {
		testUploadSequentially("", true, MIME_TYPE_XML, false, 200, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithBinResponseSequentially() throws Exception {
		testUploadSequentially("", true, MIME_TYPE_BIN, false, 200, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithJsonResponseSequentially() throws Exception {
		testUploadSequentially("", true, MIME_TYPE_JSON, false, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadSequentially() throws Exception {
		testUploadSequentially("", true, null, true, 200, createTestData(1));
	}

	@Test
	public void testUnauthorizedMultipartUploadSequentially() throws Exception {
		testUploadSequentially("", false, null, true, 401, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithXmlResponseSequentially() throws Exception {
		testUploadSequentially("", true, MIME_TYPE_XML, true, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithBinResponseSequentially() throws Exception {
		testUploadSequentially("", true, MIME_TYPE_BIN, true, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithJsonResponseSequentially() throws Exception {
		testUploadSequentially("", true, MIME_TYPE_JSON, true, 200, createTestData(1));
	}

	@Test
	public void testBulkMultipartUploadSequentially() throws Exception {
		testUploadSequentially("", true, null, true, 200, createTestData(5));
	}

	@Test
	public void testUnauthorizedBulkMultipartUploadSequentially() throws Exception {
		testUploadSequentially("", false, null, true, 401, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithXmlResponseSequentially() throws Exception {
		testUploadSequentially("", true, MIME_TYPE_XML, true, 200, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithBinResponseSequentially() throws Exception {
		testUploadSequentially("", true, MIME_TYPE_BIN, true, 200, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithJsonResponseSequentially() throws Exception {
		testUploadSequentially("", true, MIME_TYPE_JSON, true, 200, createTestData(5));
	}

	// ================================================= //
	// =========== UPLOAD TESTS - CONCURRENT =========== //
	// ================================================= //

	@Test
	public void testNonMultipartUploadConcurrently() throws Exception {
		testUploadConcurrently("", true, null, false, 200, createTestData(1));
	}

	@Test
	public void testUnauthorizedNonMultipartUploadConcurrently() throws Exception {
		testUploadConcurrently("", false, null, false, 401, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithXmlResponseConcurrently() throws Exception {
		testUploadConcurrently("", true, MIME_TYPE_XML, false, 200, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithBinResponseConcurrently() throws Exception {
		testUploadConcurrently("", true, MIME_TYPE_BIN, false, 200, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithJsonResponseConcurrently() throws Exception {
		testUploadConcurrently("", true, MIME_TYPE_JSON, false, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadConcurrently() throws Exception {
		testUploadConcurrently("", true, null, true, 200, createTestData(1));
	}

	@Test
	public void testUnauthorizedMultipartUploadConcurrently() throws Exception {
		testUploadConcurrently("", false, null, true, 401, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithXmlResponseConcurrently() throws Exception {
		testUploadConcurrently("", true, MIME_TYPE_XML, true, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithBinResponseConcurrently() throws Exception {
		testUploadConcurrently("", true, MIME_TYPE_BIN, true, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithJsonResponseConcurrently() throws Exception {
		testUploadConcurrently("", true, MIME_TYPE_JSON, true, 200, createTestData(1));
	}

	@Test
	public void testBulkMultipartUploadConcurrently() throws Exception {
		testUploadConcurrently("", true, null, true, 200, createTestData(5));
	}

	@Test
	public void testUnauthorizedBulkMultipartUploadConcurrently() throws Exception {
		testUploadConcurrently("", false, null, true, 401, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithXmlResponseConcurrently() throws Exception {
		testUploadConcurrently("", true, MIME_TYPE_XML, true, 200, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithBinResponseConcurrently() throws Exception {
		testUploadConcurrently("", true, MIME_TYPE_BIN, true, 200, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithJsonResponseConcurrently() throws Exception {
		testUploadConcurrently("", true, MIME_TYPE_JSON, true, 200, createTestData(5));
	}

	// ======================================================= //
	// =========== UPLOAD-DOWNLOAD ROUNDTRIP TESTS =========== //
	// ======================================================= //

	@Test
	public void testNonMultipartUploadAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", true, null, false, 200, createTestData(1));
	}

	@Test
	public void testUnauthorizedNonMultipartUploadAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", false, null, false, 401, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithXmlResponseAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", true, MIME_TYPE_XML, false, 200, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithBinResponseAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", true, MIME_TYPE_BIN, false, 200, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithJsonResponseAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", true, MIME_TYPE_JSON, false, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", true, null, true, 200, createTestData(1));
	}

	@Test
	public void testUnauthorizedMultipartUploadAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", false, null, true, 401, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithXmlResponseAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", true, MIME_TYPE_XML, true, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithBinResponseAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", true, MIME_TYPE_BIN, true, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithJsonResponseAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", true, MIME_TYPE_JSON, true, 200, createTestData(1));
	}

	@Test
	public void testBulkMultipartUploadAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", true, null, true, 200, createTestData(5));
	}

	@Test
	public void testUnauthorizedBulkMultipartUploadAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", false, null, true, 401, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithXmlResponseAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", true, MIME_TYPE_XML, true, 200, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithBinResponseAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", true, MIME_TYPE_BIN, true, 200, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithJsonResponseAndDownloadRoundtrip() throws Exception {
		testUploadAndDownload("", true, MIME_TYPE_JSON, true, 200, createTestData(5));
	}

	// ==================================================================== //
	// =========== UPLOAD-DOWNLOAD ROUNDTRIP TESTS - CONCURRENT =========== //
	// ==================================================================== //

	@Test
	public void testNonMultipartUploadAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", true, null, false, 200, createTestData(1));
	}

	@Test
	public void testUnauthorizedNonMultipartUploadAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", false, null, false, 401, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithXmlResponseAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", true, MIME_TYPE_XML, false, 200, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithBinResponseAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", true, MIME_TYPE_BIN, false, 200, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithJsonResponseAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", true, MIME_TYPE_JSON, false, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", true, null, true, 200, createTestData(1));
	}

	@Test
	public void testUnauthorizedMultipartUploadAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", false, null, true, 401, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithXmlResponseAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", true, MIME_TYPE_XML, true, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithBinResponseAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", true, MIME_TYPE_BIN, true, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithJsonResponseAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", true, MIME_TYPE_JSON, true, 200, createTestData(1));
	}

	@Test
	public void testBulkMultipartUploadAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", true, null, true, 200, createTestData(5));
	}

	@Test
	public void testUnauthorizedBulkMultipartUploadAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", false, null, true, 401, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithXmlResponseAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", true, MIME_TYPE_XML, true, 200, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithBinResponseAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", true, MIME_TYPE_BIN, true, 200, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithJsonResponseAndDownloadRoundtripSequentially() throws Exception {
		testUploadAndDownloadSequentially("", true, MIME_TYPE_JSON, true, 200, createTestData(5));
	}

	// ==================================================================== //
	// =========== UPLOAD-DOWNLOAD ROUNDTRIP TESTS - CONCURRENT =========== //
	// ==================================================================== //

	@Test
	public void testNonMultipartUploadAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", true, null, false, 200, createTestData(1));
	}

	@Test
	public void testUnauthorizedNonMultipartUploadAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", false, null, false, 401, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithXmlResponseAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", true, MIME_TYPE_XML, false, 200, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithBinResponseAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", true, MIME_TYPE_BIN, false, 200, createTestData(1));
	}

	@Test
	public void testNonMultipartUploadWithJsonResponseAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", true, MIME_TYPE_JSON, false, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", true, null, true, 200, createTestData(1));
	}

	@Test
	public void testUnauthorizedMultipartUploadAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", false, null, true, 401, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithXmlResponseAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", true, MIME_TYPE_XML, true, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithBinResponseAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", true, MIME_TYPE_BIN, true, 200, createTestData(1));
	}

	@Test
	public void testMultipartUploadWithJsonResponseAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", true, MIME_TYPE_JSON, true, 200, createTestData(1));
	}

	@Test
	public void testBulkMultipartUploadAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", true, null, true, 200, createTestData(5));
	}

	@Test
	public void testUnauthorizedBulkMultipartUploadAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", false, null, true, 401, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithXmlResponseAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", true, MIME_TYPE_XML, true, 200, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithBinResponseAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", true, MIME_TYPE_BIN, true, 200, createTestData(5));
	}

	@Test
	public void testBulkMultipartUploadWithJsonResponseAndDownloadRoundtripConcurrently() throws Exception {
		testUploadAndDownloadConcurrently("", true, MIME_TYPE_JSON, true, 200, createTestData(5));
	}

	// ============================= //
	// ========= COMMONS =========== //
	// ============================= //

	protected byte[] testDownload(String resourceId, String accessId, boolean withSessionId, boolean download, String downloadName,
			int expectedResponse, TestResourceData expectedFile) throws Exception {
		return testDownload(resourceId, accessId, withSessionId, download, downloadName, expectedResponse, expectedFile, null, null);
	}

	protected byte[] testDownload(String resourceId, String accessId, boolean withSessionId, boolean download, String downloadName,
			int expectedResponse, TestResourceData expectedFile, Long rangeStart, Long rangeEnd) throws Exception {

		HttpResponse resp = get(resourceId, accessId, withSessionId, download, downloadName, rangeStart, rangeEnd);

		Assert.assertEquals("Unexpected http response status code from download request", expectedResponse, resp.getStatusLine().getStatusCode());

		if (expectedResponse < 300) {

			if (downloadName != null) {
				Header contentDisp = resp.getLastHeader("Content-Disposition");
				if (contentDisp != null) {
					Assert.assertTrue(contentDisp.toString().contains(downloadName));
				}
			}

			byte[] resource = slurpResponse(resp);
			Assert.assertTrue("Unexpected downloaded resource state: " + resource, resource != null && resource.length > 0);

			if (expectedFile != null) {

				byte[] expected = expectedFile.data;

				if (rangeStart != null && rangeEnd != null) {
					int len = rangeEnd.intValue() - rangeStart.intValue();
					expected = new byte[len];
					System.arraycopy(expectedFile.data, rangeStart.intValue(), expected, 0, len);
				}

				Assert.assertArrayEquals("Unexpected resource downloaded", expected, resource);
			}

			return resource;

		} else {

			return null;

		}

	}

	protected void testDownloadConcurrently(final String resourceId, final String accessId, final boolean withSessionId, final boolean download,
			final String downloadName, final int expectedResponse, final TestResourceData expectedFile) throws Exception {
		testConcurrently(new TestCaller() {
			@Override
			public void test() throws Throwable {
				testDownload(resourceId, accessId, withSessionId, download, downloadName, expectedResponse, expectedFile);
			}
		}, MAX_CONCURRENT_TESTS);
	}

	protected void testDownloadSequentially(final String resourceId, final String accessId, final boolean withSessionId, final boolean download,
			final String downloadName, final int expectedResponse, final TestResourceData expectedFile) throws Exception {
		for (int i = 0; i < MAX_SEQUENTIAL_TESTS; i++) {
			testDownload(resourceId, accessId, withSessionId, download, downloadName, expectedResponse, expectedFile);
		}
	}

	protected List<Resource> testUploadAndDownload(String accessId, boolean withSessionId, String responseMimeType, boolean forceMultipart,
			int expectedResponse, TestResourceData... files) throws Exception {

		List<Resource> uploadedResources = testUpload(accessId, withSessionId, responseMimeType, forceMultipart, expectedResponse, files);

		if (expectedResponse < 300) {
			for (int i = 0; i < uploadedResources.size(); i++) {
				Resource resource = uploadedResources.get(i);
				testDownload(resource.getId(), accessId, withSessionId, true, null, 200, files[i]);
			}
		}

		return uploadedResources;

	}

	protected void testUploadAndDownloadConcurrently(final String accessId, final boolean withSessionId, final String responseMimeType,
			final boolean forceMultipart, final int expectedResponse, final TestResourceData... files) throws Exception {
		testConcurrently(new TestCaller() {
			@Override
			public void test() throws Throwable {
				testUploadAndDownload(accessId, withSessionId, responseMimeType, forceMultipart, expectedResponse, files);
			}
		}, MAX_CONCURRENT_TESTS);
	}

	protected void testUploadAndDownloadSequentially(final String accessId, final boolean withSessionId, final String responseMimeType,
			final boolean forceMultipart, final int expectedResponse, final TestResourceData... files) throws Exception {
		for (int i = 0; i < MAX_SEQUENTIAL_TESTS; i++) {
			testUploadAndDownload(accessId, withSessionId, responseMimeType, forceMultipart, expectedResponse, files);
		}
	}

	protected List<Resource> testUpload(String accessId, boolean withSessionId, String responseMimeType, boolean forceMultipart, int expectedResponse,
			TestResourceData... files) throws Exception {

		HttpResponse resp = post(accessId, withSessionId, responseMimeType, forceMultipart, files);

		Assert.assertEquals("Unexpected http response status code", expectedResponse, resp.getStatusLine().getStatusCode());

		if (resp.getStatusLine().getStatusCode() == 200) {

			String actualResponseMimeType = getMimeType(resp);

			if (responseMimeType != null) {
				Assert.assertEquals(responseMimeType, actualResponseMimeType);
			}

			Marshaller marshaller = marshallerRegistry.getMarshaller(actualResponseMimeType);

			List<Resource> resources = getResponse(resp, marshaller);

			Assert.assertTrue("Unexpected empty list of Resource(s) returned after upload", !resources.isEmpty());
			Assert.assertEquals("Unexpected quantity of Resource(s) returned after upload", files.length, resources.size());

			for (int i = 0; i < resources.size(); i++) {
				assertUploadedResource(resources.get(i), files[i]);
			}

			return resources;

		} else {
			consumeEntity(resp.getEntity());
			return Collections.<Resource> emptyList();
		}

	}

	protected void testUploadConcurrently(final String accessId, final boolean withSessionId, final String responseMimeType,
			final boolean forceMultipart, final int expectedResponse, final TestResourceData... files) throws Exception {
		testConcurrently(new TestCaller() {
			@Override
			public void test() throws Throwable {
				testUpload(accessId, withSessionId, responseMimeType, forceMultipart, expectedResponse, files);
			}
		}, MAX_CONCURRENT_TESTS);
	}

	protected void testUploadSequentially(final String accessId, final boolean withSessionId, final String responseMimeType,
			final boolean forceMultipart, final int expectedResponse, final TestResourceData... files) throws Exception {
		for (int i = 0; i < MAX_SEQUENTIAL_TESTS; i++) {
			testUpload(accessId, withSessionId, responseMimeType, forceMultipart, expectedResponse, files);
		}
	}

	protected HttpResponse post(String accessId, boolean withSessionId, String responseMimeType, boolean forceMultipart, TestResourceData... files)
			throws Exception {

		boolean asMultipart = (forceMultipart || files.length > 1);

		String nonMultipartFileName = (!asMultipart) ? files[0].name : null;

		URI streamingUri = assembleStreamingUri(accessId, withSessionId, nonMultipartFileName, responseMimeType);

		HttpClient client = getClient();
		HttpPost post = new HttpPost(streamingUri);

		InputStreamBody[] bodyParts = createFilePathsContents(files);

		HttpEntity entity = null;

		if (asMultipart) {
			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.setMode(HttpMultipartMode.STRICT);
			for (ContentBody bodyPart : bodyParts) {
				builder.addPart("content", bodyPart);
			}
			entity = builder.build();
		} else {
			entity = new InputStreamEntity(bodyParts[0].getInputStream());
		}

		post.setEntity(entity);

		try {
			return client.execute(post);
		} catch (Exception e) {
			consumeEntity(entity);
			throw new RuntimeException("Unable to send POST request to " + streamingUri + ": " + e.getMessage(), e);
		}

	}

	protected HttpResponse get(String resourceId, String accessId, boolean withSessionId, boolean download, String downloadName, Long rangeStart,
			Long rangeEnd) throws Exception {

		URI streamingUri = assembleStreamingUri(resourceId, accessId, withSessionId, download, downloadName);

		HttpClient client = getClient();
		HttpGet get = new HttpGet(streamingUri);

		if (rangeStart != null && rangeEnd != null) {
			String range = "bytes=".concat(rangeStart.toString()).concat("-").concat(rangeEnd < 0 ? "" : rangeEnd.toString());
			get.setHeader("Range", range);
		}

		try {
			return client.execute(get);
		} catch (Exception e) {
			throw new RuntimeException("Unable to send GET request to " + streamingUri + ": " + e.getMessage(), e);
		}
	}

	protected TestResourceData[] createTestData(int n) {
		TestResourceData[] d = new TestResourceData[n];
		for (int i = 0; i < n; i++) {
			d[i] = TestResourceDataTools.createResourceData();
		}
		return d;
	}

	protected static HttpClient getClient() {
		if (cachedClient == null) {
			try {
				cachedClient = httpClientProvider.provideHttpClient();
			} catch (Exception e) {
				throw new RuntimeException("Could not create HTTP client", e);
			}
		}
		return cachedClient;
	}

	protected static void consumeEntity(HttpEntity entity) {
		if (entity == null)
			return;

		try {
			EntityUtils.consume(entity);
		} catch (Exception e1) {
			System.err.println("Failed to consume entity: " + e1.getMessage());
			e1.printStackTrace();
		}
	}

	/**
	 * Returns the MIME type part from HttpResponse's ContentType. Character set part must not be returned.
	 */
	protected static String getMimeType(HttpResponse resp) {
		return ContentType.getOrDefault(resp.getEntity()).getMimeType();
	}

	@SuppressWarnings("unchecked")
	protected List<Resource> getResponse(HttpResponse httpResponse, Marshaller marshaller) {
		try {
			return (List<Resource>) marshaller.unmarshall(httpResponse.getEntity().getContent());
		} catch (Exception e) {
			throw new RuntimeException("Unable unmarshall  http response stream: " + e.getMessage(), e);
		}
	}

	private static InputStreamBody[] createFilePathsContents(TestResourceData... files) {
		InputStreamBody[] bodies = new InputStreamBody[files.length];
		for (int i = 0; i < files.length; i++) {
			bodies[i] = new InputStreamBody(files[i].openInputStream(), ContentType.APPLICATION_OCTET_STREAM, files[i].name);
		}
		return bodies;
	}

	private static URI assembleStreamingUri(String accessId, boolean withSessionId, String fileName, String responseMimeType) throws Exception {
		return assembleStreamingUri(null, accessId, withSessionId, false, fileName, responseMimeType);
	}

	private static URI assembleStreamingUri(String resourceId, String accessId, boolean withSessionId, boolean download, String fileName)
			throws Exception {
		return assembleStreamingUri(resourceId, accessId, withSessionId, download, fileName, null);
	}

	private static URI assembleStreamingUri(String resourceId, String accessId, boolean withSessionId, boolean download, String fileName,
			String responseMimeType) throws Exception {

		String params = buildParameters(resourceId, accessId, withSessionId ? userSessionIdProvider.get() : null, download, fileName,
				responseMimeType);

		try {
			return new URL(servletUrl + params).toURI();
		} catch (Exception e) {
			throw new RuntimeException("Unable to assemble streaming url based on " + servletUrl + ": " + e.getMessage(), e);
		}
	}

	private static String buildParameters(String resourceId, String accessId, String sessionId, boolean download, String fileName,
			String responseMimeType) {

		final Map<String, String> parameterMap = new HashMap<String, String>();
		addNotNullParameter("sessionId", sessionId, parameterMap);
		addNotNullParameter("accessId", accessId, parameterMap);
		addNotNullParameter("download", Boolean.toString(download), parameterMap);
		addNotNullParameter("resourceId", resourceId, parameterMap);
		addNotNullParameter("fileName", fileName, parameterMap);
		addNotNullParameter("responseMimeType", responseMimeType, parameterMap);

		try {
			return UrlTools.encodeQuery(parameterMap);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Failed to build query string with parameters " + parameterMap + ": " + e.getMessage(), e);
		}
	}

	private static void addNotNullParameter(String key, String value, Map<String, String> parameterMap) {
		if (value == null || value.trim().isEmpty())
			return;
		parameterMap.put(key, value);
	}

	private static byte[] slurpResponse(HttpResponse httpResponse) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
		try {
			httpResponse.getEntity().writeTo(baos);
			baos.flush();
			return baos.toByteArray();
		} catch (Exception e) {
			throw new RuntimeException("Unable to write http response: " + e.getMessage(), e);
		} finally {
			IOTools.closeQuietly(baos);
		}
	}

	private static void assertUploadedResource(Resource uploadedResource, TestResourceData expectedTestFile) {
		Assert.assertNotNull("Unexpected null uploaded Resource", uploadedResource);
		Assert.assertNotNull("Unexpected null id in uploaded Resource", uploadedResource.getId());
		Assert.assertNotNull("Unexpected null md5 in uploaded Resource", uploadedResource.getMd5());
		Assert.assertEquals("Unexpected file name in uploaded Resource", expectedTestFile.name, uploadedResource.getName());
		Assert.assertEquals("Unexpected md5 in uploaded Resource", expectedTestFile.md5, uploadedResource.getMd5());
	}

}
