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
package com.braintribe.model.processing.ddra.endpoints.api.v1;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.HttpClients;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.codec.marshaller.api.GmSerializationOptions;
import com.braintribe.codec.marshaller.api.OutputPrettiness;
import com.braintribe.codec.marshaller.api.TypeExplicitness;
import com.braintribe.codec.marshaller.api.TypeExplicitnessOption;
import com.braintribe.codec.marshaller.api.options.GmSerializationContextBuilder;
import com.braintribe.codec.marshaller.json.JsonStreamMarshaller;
import com.braintribe.exception.Exceptions;
import com.braintribe.gm.model.reason.Reason;
import com.braintribe.model.accessapi.GmqlRequest;
import com.braintribe.model.ddra.DdraUrlMethod;
import com.braintribe.model.processing.ddra.endpoints.RequestMethod;
import com.braintribe.model.processing.ddra.endpoints.TestHttpRequest;
import com.braintribe.model.processing.ddra.endpoints.TestHttpRequestFactory;
import com.braintribe.model.processing.ddra.endpoints.TestHttpResponse;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.MaybeOption;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.NeutralRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.NullRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.ResponseCodeOverridingRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestAmbigiousNestingRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestComplexServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestDeleteServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestGetServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestPostServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestPutServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestReasoningServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.TestTransformerServiceRequest;
import com.braintribe.model.processing.ddra.endpoints.api.v1.model.reason.TestReason;
import com.braintribe.model.securityservice.OpenUserSessionWithUserAndPassword;
import com.braintribe.model.service.api.result.Failure;
import com.braintribe.model.service.api.result.Neutral;
import com.braintribe.model.service.api.result.Unsatisfied;
import com.braintribe.testing.junit.assertions.assertj.core.api.Assertions;
import com.braintribe.utils.IOTools;
import com.braintribe.web.multipart.api.FormDataWriter;
import com.braintribe.web.multipart.api.MutablePartHeader;
import com.braintribe.web.multipart.api.PartWriter;
import com.braintribe.web.multipart.impl.Multiparts;

public class ApiV1RestServletBasicTest extends AbstractApiV1RestServletTest {

	public static TestServiceRequest serviceRequest(String value) {
		return serviceRequest(value, null);
	}

	public static TestComplexServiceRequest complexServiceRequest(String value) {
		TestServiceRequest testServiceRequest = serviceRequest(value, null);
		TestComplexServiceRequest request = TestComplexServiceRequest.T.create();
		request.setTestServiceRequests(Arrays.asList(testServiceRequest));

		return request;
	}

	public static TestServiceRequest serviceRequest(String value, TestServiceRequest delegate) {
		TestServiceRequest request = TestServiceRequest.T.create();
		request.setValue(value);
		if (delegate != null) {
			request.setDelegate(delegate);
		}
		return request;
	}

	private static TestTransformerServiceRequest transformerServiceRequest(String value) {
		TestTransformerServiceRequest result = TestTransformerServiceRequest.T.create();
		result.setValue(value);
		return result;
	}

	@Before
	public void before() throws Exception {
		// For some reasons the httpClient can get stuck trying to run another request after some requests.
		// Creating a new one for each test method does somehow solve this issue
		httpClient.close();
		httpClient = HttpClients.createDefault();
		requests = new TestHttpRequestFactory(httpClient, server.getServerUrl());
	}

	@Test
	public void testReasoning() {
		testReasoningWithAccept(JSON);
	}

	@Test
	public void testReasoningMultipart() {
		testReasoningWithAccept(MULTIPART, JSON);
	}

	private void testReasoningWithAccept(String... accept) {
		MaybeOption maybeOption = MaybeOption.complete;
		String completeValue = executeTestReasonRequest(maybeOption, 200, String.class, accept);

		Assert.assertEquals("Expected value.", TestReasonServiceProcessor.VALUE_COMPLETE, completeValue);

		Unsatisfied incompleteUnsatisfied = executeTestReasonRequest(MaybeOption.incomplete, 555, Unsatisfied.class, accept);

		Assert.assertEquals("Expected value.", TestReasonServiceProcessor.VALUE_INCOMPLETE, incompleteUnsatisfied.getValue());
		Assert.assertTrue("Expected value.", incompleteUnsatisfied.getHasValue());
		assertTestReason(incompleteUnsatisfied.getWhy(), TestReasonServiceProcessor.REASON_MESSSAGE_INCOMPLETE);

		Unsatisfied emptyUnsatisfied = executeTestReasonRequest(MaybeOption.empty, 500, Unsatisfied.class, accept);

		Assert.assertNull("Expected value.", emptyUnsatisfied.getValue());
		Assert.assertFalse("Expected value.", emptyUnsatisfied.getHasValue());
		assertTestReason(emptyUnsatisfied.getWhy(), TestReasonServiceProcessor.REASON_MESSSAGE_EMPTY);
	}

	private void assertTestReason(Reason reason, String message) {
		Assertions.assertThat(reason).isInstanceOf(TestReason.class);

		TestReason testReason = (TestReason) reason;

		Assertions.assertThat(testReason.getMessage()).isEqualTo(message);
	}

	private <T> T executeTestReasonRequest(MaybeOption maybeOption, int expectedStatus, Class<T> assignableTo, String... accept) {
		T value = requests.serviceGet(TestReasoningServiceRequest.T) //
				.urlParameter("maybeOption", maybeOption.name()) //
				.accept(accept) //
				.execute(expectedStatus);

		Assertions.assertThat(value).isInstanceOf(assignableTo);

		return value;
	}

	@Test
	public void testAmbigiousNestingMultipartFormData() {
		
		String boundary = Multiparts.generateBoundary();
		Map<String, String> form = new LinkedHashMap<>();
		form.put("name", "top");
		form.put("owner.name", "sub");
		
		String result = requests.servicePost(TestAmbigiousNestingRequest.T) //
				.contentType("multipart/form-data;boundary=" + boundary)
				.body(multipartFormdata(boundary, form)) //
				.accept(JSON) //
				.execute(200);
		
		Assertions.assertThat(result).isEqualTo("top:sub");
	}
	
	private byte[] multipartFormdata(String boundary, Map<String, String> form) {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			try (FormDataWriter formDataWriter = Multiparts.formDataWriter(out, boundary)) {
				for (Map.Entry<String, String> entry: form.entrySet()) {
					String name = entry.getKey();
					String value = entry.getValue();
					MutablePartHeader header = Multiparts.newPartHeader();
					header.setName(name);
					header.setContentType("text/plain");
					PartWriter part = formDataWriter.openPart(header);
					
					try (Writer writer = new OutputStreamWriter(part.outputStream(), "ISO-8859-1")) {
						writer.write(value);
					}
				}
			}
			
			return out.toByteArray();
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		catch (Exception e) {
			throw Exceptions.unchecked(e);
		}
		
	}
	
	@Test
	public void testAmbigiousNestingFormUrlEncoded() {
		String result = requests.servicePost(TestAmbigiousNestingRequest.T) //
			.contentType("application/x-www-form-urlencoded")
			.body("name=top&owner.name=sub") //
			.accept(JSON) //
			.execute(200);
		
		Assertions.assertThat(result).isEqualTo("top:sub");
	}

	@Test
	public void testAmbigiousNesting() {
		String result = requests.serviceGet(TestAmbigiousNestingRequest.T)
		.urlParameter("name", "top")
		.urlParameter("owner.name", "sub")
		.accept(JSON)
		.execute(200);
		
		Assertions.assertThat(result).isEqualTo("top:sub");
	}
	
	@Test
	public void testServiceRequestGETJson() {
		//@formatter:off
		TestServiceRequest result = requests.serviceGet(TestServiceRequest.T)
				.urlParameter("value", "testValue")
				.accept(JSON)
				.execute(200);
		//@formatter:on

		Assert.assertEquals("Expected value set on result.", "testValue", result.getValue());
	}

	@Test
	public void testServiceRequestWithNullResponse() throws Exception {
		//@formatter:off
		TestHttpResponse response = requests.serviceGet(NullRequest.T)
				.accept(JSON)
				.execute();
		//@formatter:on

		assertThat(response.getStatusCode()).isEqualTo(200);
		assertThat(response.getResponse().getEntity()).isNotNull();

		String content = IOTools.slurp(response.getResponse().getEntity().getContent(), "UTF-8");
		assertThat(content).isEqualTo("null");
	}

	@Test
	public void testServiceRequestWithNeutralResponse() throws Exception {
		//@formatter:off
		TestHttpResponse response = requests.serviceGet(NeutralRequest.T)
				.accept(JSON)
				.execute();
		//@formatter:on

		assertThat(response.getStatusCode()).isEqualTo(204);
		assertThat(response.getResponse().getEntity()).isNull();
	}

	@Test
	public void testServiceRequestWithOverriddenResponseCode() throws Exception {

		// Case 1: processor overrides response code intentionally
		ResponseCodeOverridingRequest request = ResponseCodeOverridingRequest.T.create();
		request.setResponseCode(277);
		request.setSucceed(true);

		//@formatter:off
		Neutral response = requests.servicePost(ResponseCodeOverridingRequest.T)
				.accept(JSON)
				.body(request, JSON)
				.execute(277);
		//@formatter:on

		assertThat(response).isNotNull();

	}

	@Test
	public void testServiceRequestWithOverriddenResponseCodeFails() throws Exception {

		// Case 2: processor tries to override response code intentionally but an unexpected exception occurred. Still report
		// that as 500
		ResponseCodeOverridingRequest request = ResponseCodeOverridingRequest.T.create();
		request.setSucceed(false);

		//@formatter:off
		requests.servicePost(ResponseCodeOverridingRequest.T)
				.accept(JSON)
				.body(request, JSON)
				.execute(500);
		//@formatter:on

	}

	@Test
	public void testServiceRequestGETXml() {
		//@formatter:off
		TestServiceRequest result = requests.serviceGet(TestServiceRequest.T)
				.urlParameter("value", "testValue")
				.accept(XML)
				.execute(200);
		//@formatter:on

		Assert.assertEquals("Expected value set on result.", "testValue", result.getValue());
	}

	@Test
	public void testServiceRequestPOSTJson() {
		testServiceRequestWithBody(RequestMethod.post, JSON, JSON);
		testServiceRequestWithBody(RequestMethod.post, JSON, XML);
	}

	@Test
	public void testServiceRequestPOSTXml() {
		testServiceRequestWithBody(RequestMethod.post, XML, JSON);
		testServiceRequestWithBody(RequestMethod.post, XML, XML);
	}

	@Test
	public void testComplexServiceRequestPOSTJson() {
		testComplexServiceRequestWithBody(RequestMethod.post, JSON, JSON);
		testComplexServiceRequestWithBody(RequestMethod.post, JSON, XML);
	}

	@Test
	public void testComplexServiceRequestPOSTJsonHeaderListParams() {
		testComplexServiceRequestWithHeader(RequestMethod.get, JSON, JSON);
	}

	private void testServiceRequestWithBody(RequestMethod method, String requestMimeType, String responseMimeType) {
		String value = "test_" + method.toString() + "_" + requestMimeType;

		//@formatter:off
		TestServiceRequest result = requests.request(method)
				.path("/tribefire-services/api/v1/test.access")
				.accept(responseMimeType)
				.contentType(requestMimeType)
				.body(serviceRequest(value), requestMimeType)
				.execute(200);
		//@formatter:on

		Assert.assertEquals("Expected value set on result.", value, result.getValue());
	}

	private void testComplexServiceRequestWithBody(RequestMethod method, String requestMimeType, String responseMimeType) {
		String value = "test_" + method.toString() + "_" + requestMimeType;

		//@formatter:off
		TestComplexServiceRequest result = requests.request(method)
				.path("/tribefire-services/api/v1/test.access")
				.accept(responseMimeType)
				.contentType(requestMimeType)
				.body(complexServiceRequest(value), requestMimeType)
				.execute(200);
		//@formatter:on

		Assert.assertEquals("Expected value set on result.", value, result.getTestServiceRequests().get(0).getValue());
	}

	private void testComplexServiceRequestWithHeader(RequestMethod method, String requestMimeType, String responseMimeType) {
		String value = "test_" + method.toString() + "_" + requestMimeType;

		//@formatter:off
		TestComplexServiceRequest result = requests.request(method)
				.path("/tribefire-services/api/v1/test.access/"+ TestComplexServiceRequest.T.getTypeSignature())
				.accept(responseMimeType)
				.contentType(requestMimeType)
				.header("gm-list-of-strings", "foo")
				.header("gm-list-of-strings", "bar")
				.execute(200);
		//@formatter:on

		Assert.assertEquals("Expected value set on result.", 2, result.getListOfStrings().size());
	}

	@Test
	public void testServiceRequestGETInvalidMimeType() {
		requests.serviceGet(TestServiceRequest.T).contentType(JSON).accept("invalid").execute(200);
	}

	@Test
	public void testTypeExplicitnessSettings() throws Exception {
		for (TypeExplicitness t : TypeExplicitness.values()) {
			System.out.println("Testing type explicitness setting " + t + " with infered root type.");
			testTypeExplicitnessSetting(t, true);

			System.out.println("Testing type explicitness setting " + t + " without infered root type.");
			testTypeExplicitnessSetting(t, false);
		}
	}

	private void testTypeExplicitnessSetting(TypeExplicitness typeExplicitness, boolean inferResponseType) throws IOException {
		String value = "test_";
		GmSerializationContextBuilder optionsBuilder = GmSerializationOptions.deriveDefaults() //
				.outputPrettiness(OutputPrettiness.mid) //
				.set(TypeExplicitnessOption.class, typeExplicitness); //

		if (inferResponseType) {
			optionsBuilder.inferredRootType(TestServiceRequest.T);
		}

		TestServiceRequest testServiceRequest = TestServiceRequest.T.create();
		testServiceRequest.setValue(value);
		testServiceRequest.setGlobalId("test-global-id");

		HttpResponse response = requests.serviceGet(TestServiceRequest.T) //
				.contentType(JSON) //
				.urlParameter("value", value) //
				.urlParameter("globalId", "test-global-id") //
				.urlParameter("endpoint.inferResponseType", String.valueOf(inferResponseType)) //
				.urlParameter("endpoint.typeExplicitness", typeExplicitness.name()) //
				.execute() //
				.getResponse();

		ByteArrayOutputStream responseSink = new ByteArrayOutputStream();
		IOTools.transferBytes(response.getEntity().getContent(), responseSink);
		String responseString = responseSink.toString();

		ByteArrayOutputStream expectedSink = new ByteArrayOutputStream();
		JsonStreamMarshaller marshaller = new JsonStreamMarshaller();
		marshaller.marshall(expectedSink, testServiceRequest, optionsBuilder.build());
		String expectedString = expectedSink.toString();

		assertThat(responseString).isEqualTo(expectedString);
	}

	@Test
	public void testServiceRequestGETMissingMimeType() {
		requests.serviceGet(TestServiceRequest.T).contentType(JSON).execute(200);
		requests.serviceGet(TestServiceRequest.T).contentType(JSON).accept("").execute(200);
	}

	@Test
	public void invalidUrlGET() {
		requests.get("tribefire-services/api/v1/test.access/does.not.Exists").contentType(JSON).accept(JSON).execute(404);
	}

	@Test
	public void missingServiceDomain() {
		requests.get("tribefire-services/api/v1/does.not.Exists").contentType(JSON).accept(JSON).execute(404);
	}

	/**
	 * 500 response as InvalidArgument
	 */
	@Test
	public void urlGETWithServiceDomainAndMissingTypeSignature() {
		requests.get("tribefire-services/api/v1/test.access").contentType(JSON).accept(JSON).execute(500);
	}

	/**
	 * 500 response as InvalidArgument
	 */
	@Test
	public void missingServiceDomainAndTypeSignature() {
		requests.get("tribefire-services/api/v1").contentType(JSON).accept(JSON).execute(500);
	}

	@Test
	public void testServiceRequestPOSTWithProjection() {
		testServiceRequestWithProjection(RequestMethod.post, true);
		testServiceRequestWithProjection(RequestMethod.post, false);
	}

	private void testServiceRequestWithProjection(RequestMethod method, boolean projectionInHeader) {
		String value = "test_projection_" + method.toString();

		//@formatter:off
		TestHttpRequest request = requests.request(method)
				.path("/tribefire-services/api/v1/test.access")
				.accept(JSON)
				.contentType(JSON)
				.body(serviceRequest("0", serviceRequest("1", serviceRequest(value))), JSON);
		//@formatter:on

		if (projectionInHeader) {
			request = request.header("gm-projection", "delegate.delegate.value");
		} else {
			request = request.urlParameter("projection", "delegate.delegate.value");
		}

		String result = request.execute(200);
		Assert.assertEquals("Expected result to be properly projected.", value, result);
	}

	@Test
	public void testMappingsToSameUrl() {
		setMappings(mapping("/test-request", DdraUrlMethod.GET, TestGetServiceRequest.T),
				mapping("/test-request", DdraUrlMethod.POST, TestPostServiceRequest.T),
				mapping("/test-request", DdraUrlMethod.PUT, TestPutServiceRequest.T),
				mapping("/test-request", DdraUrlMethod.DELETE, TestDeleteServiceRequest.T));

		Object result = executeMappedRequest(RequestMethod.get);
		Assert.assertTrue(result instanceof TestGetServiceRequest);
		result = executeMappedRequest(RequestMethod.post);
		Assert.assertTrue(result instanceof TestPostServiceRequest);
		result = executeMappedRequest(RequestMethod.put);
		Assert.assertTrue(result instanceof TestPutServiceRequest);
		result = executeMappedRequest(RequestMethod.delete);
		Assert.assertTrue(result instanceof TestDeleteServiceRequest);
	}

	@Test
	public void testGetPostMapping() {
		setMappings(mapping("/test-request", DdraUrlMethod.GET_POST, TestServiceRequest.T));
		Object result = executeMappedRequest(RequestMethod.get);
		Assert.assertTrue(result instanceof TestServiceRequest);
		result = executeMappedRequest(RequestMethod.post);
		Assert.assertTrue(result instanceof TestServiceRequest);
	}

	@Test
	public void testGetPostMappingAuthentication() {
		setMappings(mapping("/authenticate", DdraUrlMethod.POST, OpenUserSessionWithUserAndPassword.T, null, null, null, "test.access"));

		//@formatter:off
		TestHttpRequest request = requests.request(RequestMethod.post)
				.path("/tribefire-services/api/v1/authenticate")
				.accept(JSON)
				.contentType(JSON)
				.body("{ \"user\": \"cortex\", \"password\": \"cortex\" }");
		//@formatter:on

		OpenUserSessionWithUserAndPassword result = request.execute(200);
		Assert.assertNotNull(result);
		Assert.assertEquals("cortex", result.getUser());
		Assert.assertEquals("cortex", result.getPassword());
	}

	@Test
	public void testPostMappingAuthenticationWrongContentType() {
		setMappings(mapping("/authenticate", DdraUrlMethod.POST, OpenUserSessionWithUserAndPassword.T, null, null, null, "test.access"));

		//@formatter:off
		TestHttpRequest request = requests.request(RequestMethod.post)
				.path("/tribefire-services/api/v1/authenticate")
				.accept(JSON)
				.contentType("bar")
				.body("{ \"user\": \"cortex\", \"password\": \"cortex\" }");
		//@formatter:on

		Failure failure = request.execute(406);

		Assert.assertEquals("Unsupported Content-Type: bar.", failure.getMessage());
	}

	@Test
	public void testPostMappingAuthenticationWrongAcceptWillHandleAsJsonAnyway() {
		setMappings(mapping("/authenticate", DdraUrlMethod.POST, OpenUserSessionWithUserAndPassword.T, null, null, null, "test.access"));

		//@formatter:off
		TestHttpRequest request = requests.request(RequestMethod.post)
				.path("/tribefire-services/api/v1/authenticate")
				.accept("foo")
				.contentType(JSON)
				.body("{ \"user\": \"cortex\", \"password\": \"cortex\" }");
		//@formatter:on

		request.execute(200);
	}

	@Test
	public void testGetPostMappingAuthenticationMissingAcceptAndContentType() {
		setMappings(mapping("/authenticate", DdraUrlMethod.POST, OpenUserSessionWithUserAndPassword.T, null, null, null, "test.access"));

		//@formatter:off
		TestHttpRequest request = requests.request(RequestMethod.post)
				.path("/tribefire-services/api/v1/authenticate")
				.body("{ \"user\": \"cortex\", \"password\": \"cortex\" }");
		//@formatter:on

		OpenUserSessionWithUserAndPassword result = request.execute(200);
		Assert.assertNotNull(result);
		Assert.assertEquals("cortex", result.getUser());
		Assert.assertEquals("cortex", result.getPassword());
	}

	@Test
	public void testGetPostMappingGmql() {
		setMappings(mapping("/gmql", DdraUrlMethod.GET, GmqlRequest.T, null, null, null, "test.access"));
		String expected = "select p from Person p";
		GmqlRequest result = executeMappedRequest(RequestMethod.get, "/gmql", 200, true, "statement", expected);
		Assert.assertEquals(expected, result.getStatement());
	}

	@Test
	public void testMappingWithDeepPaths() {
		setMappings(mapping("/a/b", DdraUrlMethod.GET, TestGetServiceRequest.T), mapping("/a/c", DdraUrlMethod.GET, TestPostServiceRequest.T),
				mapping("/a/b/c", DdraUrlMethod.GET, TestPutServiceRequest.T));

		executeMappedRequest("/a", 404);

		executeMappedRequest("/a/d", 404);
		executeMappedRequest("/a/b/d", 404);
		executeMappedRequest("/a/b/c/d", 404);

		Object result = executeMappedRequest("/a/b");
		Assert.assertTrue(result instanceof TestGetServiceRequest);
		result = executeMappedRequest("/a/c");
		Assert.assertTrue(result instanceof TestPostServiceRequest);
		result = executeMappedRequest("/a/b/c");
		Assert.assertTrue(result instanceof TestPutServiceRequest);
	}

	@Test
	public void testMappingWithTransformRequest() {
		setMappings(mapping("/test-request", DdraUrlMethod.GET, TestServiceRequest.T, transformerServiceRequest("val")));

		TestTransformerServiceRequest result = executeMappedRequest();
		Assert.assertEquals("val", result.getValue());
		Assert.assertTrue(result.getServiceRequest() instanceof TestServiceRequest);
		TestServiceRequest delegate = (TestServiceRequest) result.getServiceRequest();
		Assert.assertEquals("mapped request", delegate.getValue());
	}

	@Test
	public void testMappingWithDefaultProjection() {
		setMappings(mapping("/test-request", DdraUrlMethod.GET, TestServiceRequest.T, null, "value", null));

		String result = executeMappedRequest();
		Assert.assertEquals("mapped request", result);
	}

	@Test
	public void testMappingCalledWithWrongMethod() {
		setMappings(mapping("/test-request-wm", DdraUrlMethod.GET_POST, TestServiceRequest.T),
				mapping("/test-request-wm", DdraUrlMethod.DELETE, TestServiceRequest.T));

		TestHttpResponse response = requests.request(RequestMethod.patch).path("/tribefire-services/api/v1/test-request-wm").execute();

		assertThat(response.getStatusCode()).isEqualTo(405);

		Header[] allowHeaders = response.getResponse().getHeaders("Allow");
		assertThat(allowHeaders).hasSize(1);
		assertThat(allowHeaders[0].getValue()).isEqualTo("DELETE, GET, POST");
	}

	@Test
	public void testMappingWithDefaultMimeTypeAndWildcards() {
		setMappings(mapping("/test-request-mimew", DdraUrlMethod.GET, TestServiceRequest.T, null, null, "application/xml"));
		TestHttpResponse response = requests.request(RequestMethod.get).path("/tribefire-services/api/v1/test-request-mimew").accept("*/*").execute();

		System.out.println("Recieved response");
		Assert.assertEquals(200, response.getStatusCode());
		Assert.assertEquals("application/xml", response.getResponse().getFirstHeader("Content-Type").getValue());
	}

	@Test
	public void testMappingWithDefaultMimeTypeAndWildcards2() {
		setMappings(mapping("/test-request-mimew2", DdraUrlMethod.GET, TestServiceRequest.T, null, null, "application/xml"));
		TestHttpResponse response = requests.request(RequestMethod.get).path("/tribefire-services/api/v1/test-request-mimew2").accept("application/*")
				.execute();

		System.out.println("Recieved response");
		Assert.assertEquals(200, response.getStatusCode());
		Assert.assertEquals("application/xml", response.getResponse().getFirstHeader("Content-Type").getValue());
	}

	@Test
	public void testMappingWithDefaultMimeType2() {
		setMappings(mapping("/test-request-mime2", DdraUrlMethod.GET, TestServiceRequest.T, null, null, "application/xml"));

		//@formatter:off
		TestHttpResponse response = requests.request(RequestMethod.get)
				.path("/tribefire-services/api/v1/test-request-mime2")
				.accept(JSON, "application/txt")
				.execute();
		//@formatter:on

		System.out.println("Recieved response");
		Assert.assertEquals(200, response.getStatusCode());
		Assert.assertEquals(JSON, response.getResponse().getFirstHeader("Content-Type").getValue());

	}

	@Test
	public void testMappingWithDefaultMimeType() {
		setMappings(mapping("/test-request-mime", DdraUrlMethod.GET, TestServiceRequest.T, null, null, "application/xml"));

		//@formatter:off
		TestHttpResponse response = requests.request(RequestMethod.get)
				.path("/tribefire-services/api/v1/test-request-mime")
				.accept(JSON, "application/xml") // xml is second here, but since it's a default it should be returned
				.execute();
		//@formatter:on

		System.out.println("Recieved response");
		Assert.assertEquals(200, response.getStatusCode());
		Assert.assertEquals("application/xml", response.getResponse().getFirstHeader("Content-Type").getValue());

	}

	@Test
	public void testMappingWithoutDefaultMimeTypeAndWildcardAccept() {
		setMappings(mapping("/test-request-mime", DdraUrlMethod.GET, TestServiceRequest.T, null, null, null));

		//@formatter:off
		TestHttpResponse response = requests.request(RequestMethod.get)
				.path("/tribefire-services/api/v1/test-request-mime")
				.accept("*/*")
				.execute();
		//@formatter:on

		System.out.println("Recieved response");
		Assert.assertEquals(200, response.getStatusCode());
		// As the client does not specify a prefered mime type it should be JSON
		Assert.assertEquals(JSON, response.getResponse().getFirstHeader("Content-Type").getValue());

	}

	private <T> T executeMappedRequest() {
		return executeMappedRequest(RequestMethod.get, "/test-request", 200, true, "value", "mapped request");
	}

	private <T> T executeMappedRequest(RequestMethod method) {
		return executeMappedRequest(method, "/test-request", 200, true, "value", "mapped request");
	}

	private <T> T executeMappedRequest(String path) {
		return executeMappedRequest(RequestMethod.get, path, 200, true, "value", "mapped request");
	}

	private <T> T executeMappedRequest(String path, int expectedReturnCode) {
		return executeMappedRequest(RequestMethod.get, path, expectedReturnCode, true, "value", "mapped request");
	}

	private <T> T executeMappedRequest(RequestMethod method, String path, int expectedReturnCode, boolean setValue, String paramName, String value) {
		//@formatter:off
		TestHttpRequest request = requests.request(method)
				.path("/tribefire-services/api/v1" + path)
				.accept(JSON)
				.contentType(JSON);
		//@formatter:on

		if (setValue) {
			if (method == RequestMethod.put || method == RequestMethod.post) {
				request.body("{ \"value\": \"mapped request\" }");
			} else {
				request.urlParameter(paramName, value);
			}
		}

		return request.execute(expectedReturnCode);
	}
}
