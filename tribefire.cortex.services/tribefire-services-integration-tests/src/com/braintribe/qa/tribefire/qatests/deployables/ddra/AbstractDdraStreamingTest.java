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
package com.braintribe.qa.tribefire.qatests.deployables.ddra;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.BeforeClass;
import org.junit.Test;

import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.persistence.DeleteResource;
import com.braintribe.model.resourceapi.persistence.DeleteResourceResponse;
import com.braintribe.model.resourceapi.persistence.UploadResources;
import com.braintribe.model.resourceapi.persistence.UploadResourcesResponse;
import com.braintribe.model.resourceapi.stream.GetResource;
import com.braintribe.qa.tribefire.qatests.deployables.access.AbstractPersistenceTest;
import com.braintribe.utils.IOTools;
import com.braintribe.web.multipart.api.FormDataWriter;
import com.braintribe.web.multipart.api.MutablePartHeader;
import com.braintribe.web.multipart.api.PartWriter;
import com.braintribe.web.multipart.impl.Multiparts;

public abstract class AbstractDdraStreamingTest extends AbstractPersistenceTest {

	private static final String TEST_RESOURCE_CONTENT = "TEST RESOURCE";
	private static final String ТEST_RESOURCE_LENGTH = String.valueOf(TEST_RESOURCE_CONTENT.length());
	private static final String REQUEST_URL_PREFIX = "/api/v1/";
	protected final static String TRIBEFIRE_SERVICES_URL = apiFactory().getURL();
	
	private static CloseableHttpClient httpClient;
	protected static TestHttpRequestFactory factory;
	protected static String domainId;
	protected static String sessionId;

	protected String requestUrlPrefixWithDomain() {
		return REQUEST_URL_PREFIX + domainId + "/";
	}

	@BeforeClass
	public static void beforeClass() throws MalformedURLException {
		httpClient = HttpClients.createDefault();
		factory = new TestHttpRequestFactory(httpClient, new URL(TRIBEFIRE_SERVICES_URL));
	}

	@Test
	public void testRoundTrip() throws Exception {
		String resourceId = uploadResource(TEST_RESOURCE_CONTENT);
		
		System.out.println("Uploaded Resource with ID '" + resourceId + "'.");
		
		TestHttpResponse response = downloadResourceRequest(resourceId).execute();

		assertThat(response.getStatusCode()).isEqualTo(200);
		try (InputStream in = response.getResponse().getEntity().getContent()){
			String downloaded = IOTools.slurp(in, "UTF-8");
			assertThat(downloaded).isEqualTo(TEST_RESOURCE_CONTENT);
		}
		
		deleteResource(resourceId);
	}
	
	@Test
	public void testRoundTripViaMapping() throws Exception {
		String resourceId = uploadResourceViaMapping(TEST_RESOURCE_CONTENT);
		
		System.out.println("Uploaded Resource with ID '" + resourceId + "'.");
		
		TestHttpResponse response = downloadResourceRequestViaMapping(resourceId).execute();
		
		assertThat(response.getStatusCode()).isEqualTo(200);
		try (InputStream in = response.getResponse().getEntity().getContent()){
			String downloaded = IOTools.slurp(in, "UTF-8");
			assertThat(downloaded).isEqualTo(TEST_RESOURCE_CONTENT);
		}
		
		deleteResourceViaMapping(resourceId);
	}
	
	@Test
	public void testCached() throws Exception {
		String resourceId = uploadResource(TEST_RESOURCE_CONTENT);
		
		System.out.println("Uploaded Resource with ID '" + resourceId + "'.");
		
		TestHttpResponse response = downloadResourceRequest(resourceId).execute();
		
		assertThat(response.getStatusCode()).isEqualTo(200);
		try (InputStream in = response.getResponse().getEntity().getContent()){
			String downloaded = IOTools.slurp(in, "UTF-8");
			assertThat(downloaded).isEqualTo(TEST_RESOURCE_CONTENT);
		}
		
		String eTagHeader = response.getResponse().getLastHeader("ETag").getValue();
		String acceptRangesHeader = response.getResponse().getLastHeader("Accept-Ranges").getValue();
		String contentTypeHeader = response.getResponse().getLastHeader("Content-Type").getValue();
		String contentLengthHeader = response.getResponse().getLastHeader("Content-Length").getValue();

		assertThat(eTagHeader).as("No ETag header in response of GetResource").isNotNull();
		assertThat(acceptRangesHeader).as("Wrong Accept-Ranges header in response of GetResource").isEqualTo("bytes");
		assertThat(contentTypeHeader).as("Wrong Content-Type header in response of GetResource").isEqualTo("application/text");
		assertThat(contentLengthHeader).as("Wrong Content-Length header in response of GetResource").isEqualTo(ТEST_RESOURCE_LENGTH);
		
		response = downloadResourceRequest(resourceId)
			.header("If-None-Match", eTagHeader)
			.execute();
		
		assertThat(response.getStatusCode()).isEqualTo(304);
		assertThat(response.getResponse().getEntity()).isNull();
		
		deleteResource(resourceId);
	}
	
	@Test
	public void testCachedViaMapping() throws Exception {
		String resourceId = uploadResourceViaMapping(TEST_RESOURCE_CONTENT);
		
		System.out.println("Uploaded Resource with ID '" + resourceId + "'.");
		
		TestHttpResponse response = downloadResourceRequestViaMapping(resourceId).execute();
		
		assertThat(response.getStatusCode()).isEqualTo(200);
		try (InputStream in = response.getResponse().getEntity().getContent()){
			String downloaded = IOTools.slurp(in, "UTF-8");
			assertThat(downloaded).isEqualTo(TEST_RESOURCE_CONTENT);
		}
		
		String eTagHeader = response.getResponse().getLastHeader("ETag").getValue();
		String acceptRangesHeader = response.getResponse().getLastHeader("Accept-Ranges").getValue();
		String contentTypeHeader = response.getResponse().getLastHeader("Content-Type").getValue();
		String contentLengthHeader = response.getResponse().getLastHeader("Content-Length").getValue();
		
		assertThat(eTagHeader).as("No ETag header in response of GetResource").isNotNull();
		assertThat(acceptRangesHeader).as("Wrong Accept-Ranges header in response of GetResource").isEqualTo("bytes");
		assertThat(contentTypeHeader).as("Wrong Content-Type header in response of GetResource").isEqualTo("application/text");
		assertThat(contentLengthHeader).as("Wrong Content-Length header in response of GetResource").isEqualTo(ТEST_RESOURCE_LENGTH);
		
		response = downloadResourceRequestViaMapping(resourceId)
				.header("If-None-Match", eTagHeader)
				.execute();
		
		assertThat(response.getStatusCode()).isEqualTo(304);
		assertThat(response.getResponse().getEntity()).isNull();
		
		deleteResourceViaMapping(resourceId);
	}
	
	@Test
	public void testRanged() throws Exception {
		String resourceId = uploadResource(TEST_RESOURCE_CONTENT);
		
		System.out.println("Uploaded Resource with ID '" + resourceId + "'.");
		
		TestHttpResponse response = downloadResourceRequest(resourceId)
				.header("Range", "bytes=2-9")
				.execute();
		
		assertThat(response.getStatusCode()).isEqualTo(206);
		try (InputStream in = response.getResponse().getEntity().getContent()){
			String downloaded = IOTools.slurp(in, "UTF-8");
			assertThat(downloaded).isEqualTo("ST RESOU");
		}
		
		String eTagHeader = response.getResponse().getLastHeader("ETag").getValue();
		String acceptRangesHeader = response.getResponse().getLastHeader("Accept-Ranges").getValue();
		String contentTypeHeader = response.getResponse().getLastHeader("Content-Type").getValue();
		String contentLengthHeader = response.getResponse().getLastHeader("Content-Length").getValue();
		String contentRangeHeader = response.getResponse().getLastHeader("Content-Range").getValue();
		
		
		assertThat(eTagHeader).as("No ETag header in response of GetResource").isNotNull();
		assertThat(acceptRangesHeader).as("Wrong Accept-Ranges header in response of GetResource").isEqualTo("bytes");
		assertThat(contentTypeHeader).as("Wrong Content-Type header in response of GetResource").isEqualTo("application/text");
		assertThat(contentLengthHeader).as("Wrong Content-Length header in response of GetResource").isEqualTo("8");
		assertThat(contentRangeHeader).as("Wrong Content-Range header in response of GetResource").isEqualTo("bytes 2-9/" + ТEST_RESOURCE_LENGTH);
		
		deleteResource(resourceId);
	}
	
	@Test
	public void testRangedViaMapping() throws Exception {
		String resourceId = uploadResourceViaMapping(TEST_RESOURCE_CONTENT);
		
		System.out.println("Uploaded Resource with ID '" + resourceId + "'.");
		
		TestHttpResponse response = downloadResourceRequestViaMapping(resourceId)
				.header("Range", "bytes=2-9")
				.execute();
		
		assertThat(response.getStatusCode()).isEqualTo(206);
		try (InputStream in = response.getResponse().getEntity().getContent()){
			String downloaded = IOTools.slurp(in, "UTF-8");
			assertThat(downloaded).isEqualTo("ST RESOU");
		}
		
		String eTagHeader = response.getResponse().getLastHeader("ETag").getValue();
		String acceptRangesHeader = response.getResponse().getLastHeader("Accept-Ranges").getValue();
		String contentTypeHeader = response.getResponse().getLastHeader("Content-Type").getValue();
		String contentLengthHeader = response.getResponse().getLastHeader("Content-Length").getValue();
		String contentRangeHeader = response.getResponse().getLastHeader("Content-Range").getValue();
		
		
		assertThat(eTagHeader).as("No ETag header in response of GetResource").isNotNull();
		assertThat(acceptRangesHeader).as("Wrong Accept-Ranges header in response of GetResource").isEqualTo("bytes");
		assertThat(contentTypeHeader).as("Wrong Content-Type header in response of GetResource").isEqualTo("application/text");
		assertThat(contentLengthHeader).as("Wrong Content-Length header in response of GetResource").isEqualTo("8");
		assertThat(contentRangeHeader).as("Wrong Content-Range header in response of GetResource").isEqualTo("bytes 2-9/" + ТEST_RESOURCE_LENGTH);
		
		deleteResourceViaMapping(resourceId);
	}
	
	@Test
	public void testIllegalFileSystemAccess() throws Exception {
		// GetResource should ignore passed ResourceSources entirely 
		String someValidResourceId = uploadResource(TEST_RESOURCE_CONTENT);	
		
		String responseAsString = factory.get() //
			.path(requestUrlPrefixWithDomain() + GetResource.T.getTypeSignature()) //
			.urlParameter("resource", "@r") //
			.urlParameter("@s", "com.braintribe.model.resource.source.FileSystemSource") //
			.urlParameter("s.path", "../../setup/data/config.json") //
			.urlParameter("r.resourceSource", "@s") //
			.urlParameter("r.id", someValidResourceId) //
			.urlParameter("sessionId", sessionId) //
			.urlParameter("projection", "resource") //
			.urlParameter("downloadResource", "true")
			.execute()
			.getContentStringValue();
		
		assertThat(responseAsString).isEqualTo(TEST_RESOURCE_CONTENT);
		
		// DeleteResource should ignore passed ResourceSources entirely 
		responseAsString = factory.get() //
				.path(requestUrlPrefixWithDomain() + DeleteResource.T.getTypeSignature()) //
				.urlParameter("resource", "@r") //
				.urlParameter("@s", "com.braintribe.model.resource.source.FileSystemSource") //
				.urlParameter("s.path", "../../setup/data/config.json") //
				.urlParameter("r.resourceSource", "@s") //
				.urlParameter("r.id", someValidResourceId) //
				.urlParameter("deletionScope", "source") //
				.urlParameter("sessionId", sessionId) //
				.execute()
				.getContentStringValue();
		
		System.out.println(responseAsString);
		
		Resource deletedResource = apiFactory().newSessionForAccess(domainId).query().entity(Resource.T, someValidResourceId).find();
		
		assertThat(deletedResource).isNotNull();
		assertThat(deletedResource.getResourceSource()).isNull();
	}

	private TestHttpRequest downloadResourceRequest(String resourceId) {
		return factory.get() //
				.path(requestUrlPrefixWithDomain() + GetResource.T.getTypeSignature()) //
				.urlParameter("resource.id", resourceId) //
				.urlParameter("sessionId", sessionId) //
				.urlParameter("projection", "resource") //
				.urlParameter("downloadResource", "true");
	}
	
	private void deleteResource(String resourceId) {
		DeleteResourceResponse response = factory.get() //
				.path(requestUrlPrefixWithDomain() + DeleteResource.T.getTypeSignature()) //
				.urlParameter("resource.id", resourceId) //
				.urlParameter("sessionId", sessionId)
				.execute(200);
		
		assertThat(response).isNotNull();
	}

	private String uploadResource(String testResContent) throws IOException, Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String requestBoundary = Multiparts.generateBoundary();
		prepareUploadBody(testResContent, outputStream, requestBoundary);
		
		UploadResourcesResponse result = factory.post()
				.accept("application/json")
				.path(requestUrlPrefixWithDomain() + UploadResources.T.getTypeSignature())
				.urlParameter("sessionId", sessionId)
				.header("Content-Disposition", "multipart/form-data;boundary=" + requestBoundary)
				.contentType("multipart/form-data;boundary=" + requestBoundary)
				.body(outputStream.toByteArray())
				.execute(200);
		
		assertThat(result.getResources()).hasSize(1);
		
		String resourceId = result.getResources().get(0).getId();
		return resourceId;
	}
	
	private TestHttpRequest downloadResourceRequestViaMapping(String resourceId) {
		return factory.get() //
				.path(REQUEST_URL_PREFIX + "download") //
				.urlParameter("domainId", domainId) //
				.urlParameter("resource.id", resourceId) //
				.urlParameter("sessionId", sessionId);
	}
	
	private void deleteResourceViaMapping(String resourceId) {
		DeleteResourceResponse response = factory.delete() //
				.path(REQUEST_URL_PREFIX + "delete") //
				.urlParameter("domainId", domainId) //
				.urlParameter("resource.id", resourceId) //
				.urlParameter("sessionId", sessionId)
				.execute(200);
		
		assertThat(response).isNotNull();
	}
	
	private String uploadResourceViaMapping(String testResContent) throws IOException, Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		String requestBoundary = Multiparts.generateBoundary();
		prepareUploadBody(testResContent, outputStream, requestBoundary);
		
		UploadResourcesResponse result = factory.post()
				.accept("application/json")
				.path(REQUEST_URL_PREFIX + "upload")
				.urlParameter("domainId", domainId) //
				.urlParameter("sessionId", sessionId)
				.header("Content-Disposition", "multipart/form-data;boundary=" + requestBoundary)
				.contentType("multipart/form-data;boundary=" + requestBoundary)
				.body(outputStream.toByteArray())
				.execute(200);
		
		assertThat(result.getResources()).hasSize(1);
		
		String resourceId = result.getResources().get(0).getId();
		return resourceId;
	}

	private void prepareUploadBody(String testResContent, ByteArrayOutputStream outputStream, String requestBoundary) throws IOException, Exception {
		try (FormDataWriter formDataWriter = Multiparts.formDataWriter(outputStream, requestBoundary)){
			MutablePartHeader header = Multiparts.newPartHeader();
			header.setName("resources");
			header.setContentType("application/text");
			PartWriter part = formDataWriter.openPart(header);
			
			try (OutputStream resOut = part.outputStream()){
				resOut.write(testResContent.getBytes());
			}
		}
	}

}