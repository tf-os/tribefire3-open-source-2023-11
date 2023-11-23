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
package com.braintribe.model.openapi.v3_0.export.legacytests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.braintribe.model.ddra.DdraConfiguration;
import com.braintribe.model.ddra.DdraMapping;
import com.braintribe.model.ddra.DdraUrlMethod;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.openapi.v3_0.OpenApi;
import com.braintribe.model.openapi.v3_0.OpenapiOperation;
import com.braintribe.model.openapi.v3_0.OpenapiPath;
import com.braintribe.model.openapi.v3_0.OpenapiResponse;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.model.openapi.v3_0.OpenapiType;
import com.braintribe.model.openapi.v3_0.api.OpenapiServicesRequest;
import com.braintribe.model.openapi.v3_0.export.AbstractOpenapiProcessor;
import com.braintribe.model.openapi.v3_0.export.ApiV1OpenapiProcessor;
import com.braintribe.model.openapi.v3_0.export.legacytests.ParameterAssertion.CollectionParameterAssertion;
import com.braintribe.model.openapi.v3_0.export.legacytests.ParameterAssertion.DefaultEndpoints;
import com.braintribe.model.openapi.v3_0.export.legacytests.ParameterAssertion.SimpleParameterAssertion;
import com.braintribe.model.openapi.v3_0.export.legacytests.ioc.TestAccessSpace;
import com.braintribe.model.openapi.v3_0.export.legacytests.model.TestAccessRequest;
import com.braintribe.model.openapi.v3_0.export.legacytests.model.TestServiceRequestWithEntityProperty;
import com.braintribe.model.openapi.v3_0.export.legacytests.model2.TestEnum;
import com.braintribe.model.openapi.v3_0.export.legacytests.model2.TestServiceMetadataRequest;
import com.braintribe.model.openapi.v3_0.export.legacytests.model2.TestServiceRequest;
import com.braintribe.model.openapi.v3_0.export.legacytests.model2.TestServiceRequestExtended;
import com.braintribe.model.openapi.v3_0.export.legacytests.model2.TestServiceResponse;
import com.braintribe.model.openapi.v3_0.export.legacytests.model2.TestServiceSimplepropsRequest;
import com.braintribe.model.openapi.v3_0.export.legacytests.wire.ApiV1OpenapiProcessorTestWireModule;
import com.braintribe.model.openapi.v3_0.export.legacytests.wire.contract.ApiV1OpenapiProcessorTestContract;
import com.braintribe.model.processing.web.rest.impl.HttpRequestEntityDecoderImpl;
import com.braintribe.wire.api.module.WireTerminalModule;

public class ApiV1OpenapiProcessorTest extends AbstractOpenapiProcessorTest<ApiV1OpenapiProcessorTestContract> {

	private static final String DEFAULT_TEST_DOMAIN = "test.domain2";

	public List<SimpleParameterAssertion> defaultRequestParameters(String in) {
		return Arrays.asList(
				// The mandatory property is first even though alphabetically it comes later
				new SimpleParameterAssertion("mandatoryProperty", in, OpenapiType.STRING, true, null),
				new SimpleParameterAssertion("bigDecimalProperty", in, OpenapiType.NUMBER, false, null),
				new SimpleParameterAssertion("intProperty", in, OpenapiType.INTEGER, false, 42),
				new SimpleParameterAssertion("propertyWithInitializer", in, OpenapiType.STRING, false, "test value"));
	}

	public List<SimpleParameterAssertion> defaultRequestParametersExtended(String in) {
		List<SimpleParameterAssertion> extended = new ArrayList<>();

		extended.addAll(defaultRequestParameters(in));
		extended.add(new CollectionParameterAssertion("stringList", in, false, OpenapiType.STRING));
		
		// first because high priority
		extended.add(0, new SimpleParameterAssertion("veryHighPriorityProperty", in, OpenapiType.STRING, false, null));

		// because of alphabetic order this parameter is at index 4
		extended.add(4, new CollectionParameterAssertion("intSet", in, false, OpenapiType.INTEGER));
		
		// last because low priority
		extended.add(new SimpleParameterAssertion("lowPriorityProperty", in, OpenapiType.STRING, false, null));
		
		// (invisibleProperty not listed because it has "Hidden" metadata)
		// (stringMap and objectList properties not listed because their types are not supported)
		
		return extended;
	}

	private final SchemaAsserter testServiceRequestScheme = SchemaAsserter //
			.schema(TestServiceRequest.T) //
			.stringProperty("mandatoryProperty", true) //
			.stringProperty("propertyWithInitializer", false, "test value") //
			.integerProperty("intProperty", false, 42) //
			.property("bigDecimalProperty", SchemaAsserter.schema("decimal", OpenapiType.NUMBER), false); //
//			.property("metaData", //
//					SchemaAsserter.schema("map", OpenapiType.OBJECT).additionalProperties(), //
//					false);
	private final SchemaAsserter testServiceRequestSchemeMultipart = testServiceRequestScheme.clone(TestServiceRequest.T)
			.property(HttpRequestEntityDecoderImpl.SERIALIZED_REQUEST, SchemaAsserter.objectSchema(), false);
	
	@Before
	public void init(){
//		testServiceRequestScheme.property("delegate", testServiceRequestScheme, false);
	}

	@Test
	public void processGet() {
		String in = "query";

		OpenApi api = runProcessor();
		OpenapiPath path = getPath(api);

		OpenapiOperation get = path.getGet();

		ParameterAsserter.start() //
				.expect(defaultRequestParameters(in)) //
				.expect(DefaultEndpoints.all) //
				.in(get);

		assertResponseSchema(get);
	}

	@Test
	public void processGetExtended() {
		OpenApi api = runProcessor();
		OpenapiPath path = getPath(api, DEFAULT_TEST_DOMAIN, TestServiceRequestExtended.T);

		OpenapiOperation get = path.getGet();

		ParameterAsserter.start() //
				.expect(defaultRequestParametersExtended("query")) //
				.expect(DefaultEndpoints.all) //
				.in(get);

		assertResponseSchema(get);
	}

	@Test
	public void processPut() {
		OpenApi api = runProcessor(mapping(DdraUrlMethod.PUT));
		Map<String, OpenapiPath> paths = api.getPaths();
		OpenapiPath path = paths.get("/test-path");
		Assert.assertNotNull(path);

		OpenapiOperation put = path.getPut();

		ParameterAsserter.start() //
			.expectRequestBody(APPLICATION_JSON, testServiceRequestScheme) //
			.expectRequestBody(AbstractOpenapiProcessor.ALL_MEDIA_TYPES_RANGE, testServiceRequestScheme) //
			.expect(DefaultEndpoints.all) //
			.in(put);
	}

	@Test
	@Ignore // Probably this mapping doesnt make sense in the new UI where both can be shown next to each other?
	public void processPostSimpleBodyBecauseOfMapping() {
		DdraMapping mappingPostSimpleBody = mapping(DdraUrlMethod.POST);
		mappingPostSimpleBody.setAnnounceAsMultipart(false);

		OpenApi api = runProcessor(mappingPostSimpleBody);
		Map<String, OpenapiPath> paths = api.getPaths();
		OpenapiPath path = paths.get("/test-path");
		Assert.assertNotNull(path);

		OpenapiOperation post = path.getPost();

		ParameterAsserter.start() //
			.expectRequestBody(APPLICATION_JSON, testServiceRequestScheme) //
			.expectRequestBody(MULTIPART_FORM_DATA, testServiceRequestSchemeMultipart) //
			.expect(DefaultEndpoints.all) //
			.in(post);
	}
	
	@Test
	public void processPostSimpleBodyBecauseOfRequest() {
		OpenapiServicesRequest swaggerRequest = OpenapiServicesRequest.T.create();
		swaggerRequest.setServiceDomain(DEFAULT_TEST_DOMAIN);
		swaggerRequest.setDefaultToMultipart(false);
		
		OpenApi api = runProcessor(swaggerRequest);
		OpenapiPath path = getPath(api);

		OpenapiOperation post = path.getPost();
		
		ParameterAsserter.start() //
			.expectRequestBody(APPLICATION_JSON, testServiceRequestScheme) //
			.expectRequestBody(MULTIPART_FORM_DATA, testServiceRequestSchemeMultipart) //
			.expectRequestBody(URLENCODED, null) // TODO: test properly
			.expect(DefaultEndpoints.all) //
			.in(post);
	}

	@Test
	public void processPost() {
		OpenApi api = runProcessor();
		OpenapiPath path = getPath(api);

		OpenapiOperation post = path.getPost();

		ParameterAsserter.start() //
				.expectRequestBody(MULTIPART_FORM_DATA, testServiceRequestSchemeMultipart)
				.expectRequestBody(APPLICATION_JSON, testServiceRequestScheme)
				.expectRequestBody(URLENCODED, null) // TODO: test properly
				.expect(DefaultEndpoints.all) //
				.in(post);

		assertResponseSchema(post);

	}

	@Test
	public void processPostExtended() {
		OpenApi api = runProcessor();
		OpenapiPath path = getPath(api, DEFAULT_TEST_DOMAIN, TestServiceRequestExtended.T);

		OpenapiOperation post = path.getPost();
		
		SchemaAsserter stringSchema = SchemaAsserter.schema(null, OpenapiType.STRING);
		SchemaAsserter stringMapSchema = SchemaAsserter.schema("map", OpenapiType.OBJECT).additionalProperties(stringSchema);
		
		SchemaAsserter multipartScheme = testServiceRequestScheme.clone(TestServiceRequestExtended.T)
			.listProperty("stringList", stringSchema, false)
			.setProperty("intSet", SchemaAsserter.intSchema(), false)
			.stringProperty("veryHighPriorityProperty", false)
			.stringProperty("lowPriorityProperty", false);

		SchemaAsserter jsonScheme = multipartScheme.clone(TestServiceRequestExtended.T)
			.listProperty("objectList", SchemaAsserter.schema("object", OpenapiType.OBJECT), false)
			.property("stringMap", stringMapSchema, false);
		
		multipartScheme.property(HttpRequestEntityDecoderImpl.SERIALIZED_REQUEST, SchemaAsserter.objectSchema(), false);
			
		ParameterAsserter.start() //
				.expectRequestBody(MULTIPART_FORM_DATA, multipartScheme)
				.expectRequestBody(APPLICATION_JSON, jsonScheme)
				.expectRequestBody(URLENCODED, null) // TODO: test properly
				.expect(DefaultEndpoints.all) //
				.in(post);

		assertResponseSchema(post);

	}

	@Test
	public void processMapping() {
		OpenApi api = runProcessor(mapping(DdraUrlMethod.DELETE));
		OpenapiPath path = api.getPaths().get("/test-path");
		Assert.assertNotNull(path);
		OpenapiOperation delete = path.getDelete();

		ParameterAsserter.start() //
				.expect(defaultRequestParameters("query")) //
				.expect(DefaultEndpoints.all) //
				.in(delete);

		assertResponseSchema(delete);

	}

	@Test
	@Ignore // default mappings should be passed via endpoint prototype / not supported for now
	public void processMappingWithDefaultMappingValues() {
		DdraMapping mappingDelete = mapping(DdraUrlMethod.DELETE);
		mappingDelete.setDefaultDepth("0");
		mappingDelete.setDefaultMimeType("plain/text");
		mappingDelete.setDefaultProjection("envelop");
		mappingDelete.setDefaultPrettiness("high");
		mappingDelete.setDefaultStabilizeOrder(true);
		mappingDelete.setDefaultWriteEmptyProperties(true);
		mappingDelete.setDefaultTypeExplicitness("always");
		mappingDelete.setDefaultEntityRecurrenceDepth(1);

		OpenApi api = runProcessor(mappingDelete);
		OpenapiPath path = api.getPaths().get("/test-path");
		Assert.assertNotNull(path);
		OpenapiOperation delete = path.getDelete();

		Map<String, String> newDefaults = new HashMap<>();
		newDefaults.put("endpoint.depth", "0");
		newDefaults.put("endpoint.projection", "envelop");
		newDefaults.put("endpoint.prettiness", "high");
		newDefaults.put("endpoint.stabilizeOrder", "true");
		newDefaults.put("endpoint.writeEmptyProperties", "true");
		newDefaults.put("endpoint.typeExplicitness", "always");
		newDefaults.put("endpoint.entityRecurrenceDepth", "1");

		List<ParameterAssertion<?>> mappedEndpoints = DefaultEndpoints.switchDefault(newDefaults, DefaultEndpoints.all);

		ParameterAsserter.start() //
				.expect(defaultRequestParameters("query")) //
				.expect(mappedEndpoints) //
				.in(delete);

		assertResponseSchema(delete);

	}
	
	@Test
	public void processMappingWithSpecificMetadata() {
		DdraMapping mappingDelete = mapping(DdraUrlMethod.DELETE, TestServiceMetadataRequest.T);
		mappingDelete.setPath("/meta/on/endpoint");
		
		OpenApi api = runProcessor(mappingDelete);
		OpenapiPath path = api.getPaths().get("/meta/on/endpoint");
		Assert.assertNotNull(path);
		OpenapiOperation delete = path.getDelete();
		
		List<ParameterAssertion<?>> endpoints = new ArrayList<>(DefaultEndpoints.all);
		endpoints.remove(DefaultEndpoints.depth);
		
		List<SimpleParameterAssertion> requestParameters = new ArrayList<>(defaultRequestParameters("query"));
		requestParameters.remove(3);
		
		ParameterAsserter.start() //
			.expect(requestParameters) //
			.expect(endpoints) //
			.in(delete);
		
		assertResponseSchema(delete);
		
		// mapping-specific metadata shouldn't have had effect on other requests
		path = getPath(api, DEFAULT_TEST_DOMAIN, TestServiceRequest.T);
		Assert.assertNotNull(path);
		OpenapiOperation get = path.getGet();
		
		ParameterAsserter.start() //
			.expect(defaultRequestParameters("query")) //
			.expect(DefaultEndpoints.all) //
			.in(get);
		
		assertResponseSchema(get);
		
	}
	
	@Test
	public void processUnmappedWithSpecificMetadata() {
		OpenApi api = runProcessor();
		OpenapiPath path = getPath(api, DEFAULT_TEST_DOMAIN, TestServiceMetadataRequest.T);
		Assert.assertNotNull(path);
		OpenapiOperation get = path.getGet();
		
		// This request has a specific metadata added on the Ap1V1DdraEndpoint's 'projection' property 
		List<ParameterAssertion<?>> endpoints = new ArrayList<>(DefaultEndpoints.all);
		endpoints.remove(DefaultEndpoints.projection);
		
		List<ParameterAssertion<?>> requestParameters = new ArrayList<>(defaultRequestParameters("query"));
		requestParameters.remove(1);
		
		ParameterAsserter.start() //
			.expect(requestParameters) //
			.expect(endpoints) //
			.in(get);
		
		assertResponseSchema(get);
		
	}

	@Test
	public void testEmbedded() {
		OpenApi api = runProcessor("test.access");
		OpenapiPath path = getPath(api, "test.access", TestServiceRequestWithEntityProperty.T);
		Assert.assertNotNull(path);
		OpenapiOperation post = path.getPost();

		SchemaAsserter requestBodySchemeFormData = SchemaAsserter.schema(TestServiceRequestWithEntityProperty.T)
			.listProperty("listOfStrings", SchemaAsserter.schema(null, OpenapiType.STRING), false)
			.stringProperty("zipRequest.name", true)
			.property("zipRequest.resource", SchemaAsserter.resourceSchema(), false)
			.property(HttpRequestEntityDecoderImpl.SERIALIZED_REQUEST, SchemaAsserter.objectSchema(), false);
		
		
		ParameterAsserter.start() //
			.expect(DefaultEndpoints.all) //
			.expectRequestBody(APPLICATION_JSON, null)
			.expectRequestBody(MULTIPART_FORM_DATA, requestBodySchemeFormData)
			.expectRequestBody(URLENCODED, null) // TODO: test properly
			.in(post);
		
		OpenapiOperation get = path.getGet();
		
		ParameterAsserter.start() //
			.expect("listOfStrings", "query", OpenapiType.ARRAY, false, null) //
			.expect("zipRequest.name", "query", OpenapiType.STRING, true, null) //
			.expect(DefaultEndpoints.all) //
			.in(get);
		
		// ----------
		
		path = getPath(api, "test.access", TestAccessRequest.T);
		
		Assert.assertNotNull(path);
		post = path.getPost();
		
		requestBodySchemeFormData = SchemaAsserter.schema(TestAccessRequest.T)
			.property("thing.id", SchemaAsserter.objectSchema(), false)
			.property(HttpRequestEntityDecoderImpl.SERIALIZED_REQUEST, SchemaAsserter.objectSchema(), false);
		
		ParameterAsserter.start() //
			.expect(DefaultEndpoints.all) //
			.expectRequestBody(APPLICATION_JSON, null)
			.expectRequestBody(MULTIPART_FORM_DATA, requestBodySchemeFormData)
			.expectRequestBody(URLENCODED, null) // TODO: test properly
			.in(post);
		
		get = path.getGet();
		
		ParameterAsserter.start() //
			.expect("thing.id", "query", OpenapiType.OBJECT, false, null) //
			.expect(DefaultEndpoints.all) //
			.in(get);
	}
	
	@Test
	public void testSimpleProps() {
		OpenApi api = runProcessor(DEFAULT_TEST_DOMAIN);
		OpenapiPath path = getPath(api, DEFAULT_TEST_DOMAIN, TestServiceSimplepropsRequest.T);
		Assert.assertNotNull(path);
		OpenapiOperation post = path.getPost();
		
		SchemaAsserter requestBodySchemaAsserterJson = SchemaAsserter.schema(TestServiceSimplepropsRequest.T)
			.booleanProperty("boolProperty", false, null)
			.booleanProperty("booleanProperty", false)
			.integerProperty("integerProperty", false, null)
			.integerProperty("intProperty", false);
		
		SchemaAsserter requestBodySchemaAsserterMultipart = requestBodySchemaAsserterJson.clone(TestServiceSimplepropsRequest.T)
			.property(HttpRequestEntityDecoderImpl.SERIALIZED_REQUEST, SchemaAsserter.objectSchema(), false);

		ParameterAsserter.start() //
//			.expectSerializedRequest() //
			.expect(DefaultEndpoints.all) //
			.expectRequestBody(APPLICATION_JSON, requestBodySchemaAsserterJson)
			.expectRequestBody(MULTIPART_FORM_DATA, requestBodySchemaAsserterMultipart)
			.expectRequestBody(URLENCODED, null) // TODO: test properly
			.in(post);
		
		OpenapiOperation get = path.getGet();
		
		ParameterAsserter.start() //
			.expect("boolProperty", "query", OpenapiType.BOOLEAN, false, null) //
			.expect("booleanProperty", "query", OpenapiType.BOOLEAN, false, false) //
			.expect("intProperty", "query", OpenapiType.INTEGER, false, 0) //
			.expect("integerProperty", "query", OpenapiType.INTEGER, false, null) //
			.expect(DefaultEndpoints.all) //
			.in(get);
	}
	
	private OpenApi runProcessor(DdraMapping... mappings) {
		return runProcessor(DEFAULT_TEST_DOMAIN, mappings);
	}
	
	@Override
	protected WireTerminalModule<ApiV1OpenapiProcessorTestContract> module() {
		return ApiV1OpenapiProcessorTestWireModule.INSTANCE;
	}

	private OpenApi runProcessor(String serviceDomain, DdraMapping... mappings) {
		OpenapiServicesRequest request = OpenapiServicesRequest.T.create();
		request.setServiceDomain(serviceDomain);
		return runProcessor(request, mappings);
	}
	
	private OpenApi runProcessor(OpenapiServicesRequest request, DdraMapping... mappings) {
		DdraConfiguration ddraConfiguration = contract.ddraConfiguration();
		ddraConfiguration.getMappings().addAll(Arrays.asList(mappings));
		
		ddraConfiguration.getMappings().add(createDummyMapping());
		
//		processor.setEvaluator(new TestSwaggerEvaluator());
		OpenApi result = request.eval(evaluator).get();
		components = result.getComponents();
		
		Assert.assertEquals("3.0.1", result.getOpenapi());
		Assert.assertNotNull(result.getInfo());
		
		if (request.getServiceDomain() == DEFAULT_TEST_DOMAIN) {
			assertThat(result.getInfo().getDescription()).isEqualTo(TestAccessSpace.CUSTOM_SERVICE_MODEL_DESCRIPTION);
			assertThat(result.getInfo().getTitle()).isEqualTo(TestAccessSpace.CUSTOM_SERVICE_MODEL_NAME);
		}
		
		return result;
	}

	// create dummy mapping that should be ignored because it's not in the test domain
	private DdraMapping createDummyMapping() {
		DdraMapping dummyMapping = DdraMapping.T.create();
		dummyMapping.setMethod(DdraUrlMethod.DELETE);
		dummyMapping.setPath("/dummy-path");

		GmEntityType requestGmType = GmEntityType.T.create();
		requestGmType.setTypeSignature(TestServiceRequest.T.getTypeSignature());
		dummyMapping.setRequestType(requestGmType);
		return dummyMapping;
	}

	private OpenapiPath getPath(OpenApi api, String domainId, EntityType<?> requestType) {
		Map<String, OpenapiPath> paths = api.getPaths();
		OpenapiPath path = paths.get("/" + domainId + "/" + requestType.getShortName());
		Assert.assertNotNull(path);

		return path;
	}

	private OpenapiPath getPath(OpenApi api) {
		return getPath(api, DEFAULT_TEST_DOMAIN, TestServiceRequest.T);
	}

	private void assertResponseSchema(OpenapiOperation operation) {
		OpenapiResponse response = getReferenced(operation.getResponses().get("200"));
		Assert.assertNotNull(response);
		OpenapiSchema responseSchema = response.getContent().get(ApiV1OpenapiProcessor.ALL_MEDIA_TYPES_RANGE).getSchema();
		SchemaAsserter //
				.schema(TestServiceResponse.T) //
				.stringProperty("stringProperty", false) //
				.integerProperty("intProperty", true) //
				.enumProperty("enumProperty", false, TestEnum.class) //
				.testUnwrapped(responseSchema);
	}

	private static DdraMapping mapping(DdraUrlMethod method) {
		return mapping(method, TestServiceRequest.T);
	}

	private static DdraMapping mapping(DdraUrlMethod method, EntityType<?> requestType) {
		DdraMapping mapping = DdraMapping.T.create();

		mapping.setMethod(method);
		mapping.setPath("/test-path");
		mapping.setDefaultServiceDomain(DEFAULT_TEST_DOMAIN);

		GmEntityType requestGmType = GmEntityType.T.create();
		requestGmType.setTypeSignature(requestType.getTypeSignature());
		mapping.setRequestType(requestGmType);

		return mapping;
	}

}
