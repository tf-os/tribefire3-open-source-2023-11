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

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.braintribe.model.accessapi.ManipulationResponse;
import com.braintribe.model.openapi.v3_0.OpenApi;
import com.braintribe.model.openapi.v3_0.OpenapiMediaType;
import com.braintribe.model.openapi.v3_0.OpenapiOperation;
import com.braintribe.model.openapi.v3_0.OpenapiPath;
import com.braintribe.model.openapi.v3_0.OpenapiRequestBody;
import com.braintribe.model.openapi.v3_0.OpenapiResponse;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.model.openapi.v3_0.OpenapiType;
import com.braintribe.model.openapi.v3_0.api.OpenapiEntitiesRequest;
import com.braintribe.model.openapi.v3_0.export.legacytests.model2.FooEntity;
import com.braintribe.model.openapi.v3_0.export.legacytests.wire.EntitiesOpenapiProcessorTestWireModule;
import com.braintribe.model.openapi.v3_0.export.legacytests.wire.contract.EntitiesOpenapiProcessorTestContract;
import com.braintribe.model.query.EntityQueryResult;
import com.braintribe.model.user.Group;
import com.braintribe.model.user.Role;
import com.braintribe.model.user.User;
import com.braintribe.wire.api.module.WireTerminalModule;

public class EntitiesOpenapiProcessorTest extends AbstractOpenapiProcessorTest<EntitiesOpenapiProcessorTestContract> {

	private static final String PATH_USER = "/com.braintribe.model.user.User";
	private static final String PATH_USER_WITH_ID = "/com.braintribe.model.user.User/{id}";
	private static final String PATH_USER_WITH_ID_AND_PARTITION = "/com.braintribe.model.user.User/{id}/{partition}";

	@Override
	protected WireTerminalModule<EntitiesOpenapiProcessorTestContract> module() {
		return EntitiesOpenapiProcessorTestWireModule.INSTANCE;
	}

	@Test
	public void testSwaggerEntitiesPath() {
		OpenApi api = runProcessor(getSwaggerForEntitiesWithTestAccess());
		OpenapiPath path = getPath(api, PATH_USER);

		// get
		assertOperation(path.getGet(), false, false, false);

		// delete
		assertOperation(path.getDelete(), false, false, false);
		assertSimpleParameter(path.getDelete(), "endpoint.deleteMode", "query", "string", false, "dropReferencesIfPossible");
		assertSimpleParameter(path.getDelete(), "endpoint.allowMultipleDelete", "query", "boolean", false, false);

		// post
		assertOperation(path.getPost(), true, false, false);
		assertRequestBodySchemaIsUserEntity(path.getPost().getRequestBody());

		// put
		assertOperation(path.getPut(), true, false, false);
		assertRequestBodySchemaIsUserEntity(path.getPut().getRequestBody());

		assertResponseSchemas(path);
	}

	@Test
	public void testSwaggerEntitiesPathWithId() {
		OpenApi api = runProcessor(getSwaggerForEntitiesWithTestAccess());
		OpenapiPath path = getPath(api, PATH_USER_WITH_ID);
		Assert.assertNull(api.getPaths().get("/" + FooEntity.T.getTypeSignature()));

		// get
		assertOperation(path.getGet(), false, true, false);
		assertSimpleParameter(path.getGet(), "endpoint.projection", "query", "string", false, null);

		// delete
		assertOperation(path.getDelete(), false, true, false);
		assertSimpleParameter(path.getDelete(), "endpoint.deleteMode", "query", "string", false, "dropReferencesIfPossible");

		// post
		assertOperation(path.getPost(), true, true, false);
		assertSimpleParameter(path.getPost(), "endpoint.projection", "query", "string", false, "idInfo");
		assertRequestBodySchemaIsUserEntity(path.getPost().getRequestBody());

		// put
		assertOperation(path.getPut(), true, true, false);
		assertSimpleParameter(path.getPut(), "endpoint.projection", "query", "string", false, "idInfo");
		assertRequestBodySchemaIsUserEntity(path.getPut().getRequestBody());

		// patch
		assertOperation(path.getPatch(), true, true, false);
		assertSimpleParameter(path.getPatch(), "endpoint.projection", "query", "string", false, "idInfo");
		assertRequestBodySchemaIsUserEntity(path.getPatch().getRequestBody());

		assertResponseSchemas(path);
	}

	@Test
	public void testSwaggerEntitiesPathWithIdAndPartition() {
		OpenapiEntitiesRequest request = getSwaggerForEntitiesWithTestAccess();
		request.setEnablePartition(true);
		OpenApi api = runProcessor(request);
		OpenapiPath path = getPath(api, PATH_USER_WITH_ID_AND_PARTITION);
		Assert.assertNull(api.getPaths().get("/" + FooEntity.T.getTypeSignature()));

		// get
		assertOperation(path.getGet(), false, true, true);

		// delete
		assertOperation(path.getDelete(), false, true, true);
		assertSimpleParameter(path.getDelete(), "endpoint.deleteMode", "query", "string", false, "dropReferencesIfPossible");

		// post
		assertOperation(path.getPost(), true, true, true);
		assertRequestBodySchemaIsUserEntity(path.getPost().getRequestBody());

		// put
		assertOperation(path.getPut(), true, true, true);
		assertRequestBodySchemaIsUserEntity(path.getPut().getRequestBody());

		assertResponseSchemas(path);
	}

	@Test
	public void testSwaggerEntitiesGetAll_MultipleAccesses() {
		OpenapiEntitiesRequest create = OpenapiEntitiesRequest.T.create();
		create.setAccessId("test.access");
		// create.setBasePath("/tribefire-services/rest/v2/entities/test.access");
		OpenApi api = runProcessor(create);

		getPath(api, "/" + User.T.getTypeSignature());
		getPath(api, "/" + Role.T.getTypeSignature());
		getPath(api, "/" + Group.T.getTypeSignature());

		// We are not supporting multiple accesses in one Swagger definition anymore.
		Assert.assertNull(api.getPaths().get("/entities/test.access.two/" + FooEntity.T.getTypeSignature()));
		// getPath(api, "/entities/test.access.two/" + FooEntity.T.getTypeSignature());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSwaggerEntitiesGetAll_WrongAccesses() {
		OpenapiEntitiesRequest request = OpenapiEntitiesRequest.T.create();
		request.setAccessId("wrong.access");
		OpenApi api = runProcessor(request);

		Assert.assertEquals(0, api.getPaths().size());
	}

	private void assertRequestBodySchemaIsUserEntity(OpenapiRequestBody body) {
		Map<String, OpenapiMediaType> content = getReferenced(body).getContent();

		OpenapiSchema schema = getSchemaFromContent(content);
		assertSchemaProperty(schema, "name", null, OpenapiType.STRING, true); // name property of user entity
	}

	private OpenapiEntitiesRequest getSwaggerForEntitiesWithTestAccess() {
		OpenapiEntitiesRequest request = OpenapiEntitiesRequest.T.create();
		request.setAccessId("test.access");
		return request;
	}

	private void assertOperation(OpenapiOperation operation, boolean isPostOrPut, boolean withPathId,
			boolean withPathPartition) {
		Assert.assertNotNull(operation);
		assertEndpointParameters(operation);
		assertIdAndPartition(operation, withPathId, withPathPartition);
		if (!isPostOrPut) {
			if (!withPathId) {
				assertSimpleParameter(operation, "where.name", "query", "string", false, null);
			}
		}

	}

	private OpenApi runProcessor(OpenapiEntitiesRequest request) {

		// processor.setEvaluator(new TestSwaggerV2Evaluator());
		OpenApi result = request.eval(evaluator).get();
		components = result.getComponents();

		Assert.assertEquals("3.0.1", result.getOpenapi());

		// String comparison = (request.getBasePath() != null) ? request.getBasePath() : "/tribefire-services/rest/v2/"
		// + request.getAccessId();
		// Assert.assertEquals(comparison, result.getBasePath());
		Assert.assertNotNull(result.getInfo());

		return result;
	}

	private void assertResponseSchemas(OpenapiPath openapiPath) {
		assertSuccessfulResponseSchema(openapiPath.getGet(), EntityQueryResult.T.getTypeSignature(), OpenapiType.OBJECT);
		assertSuccessfulResponseSchema(openapiPath.getDelete(), null, OpenapiType.INTEGER);
		assertSuccessfulResponseSchema(openapiPath.getPut(), ManipulationResponse.T.getTypeSignature(), OpenapiType.OBJECT);
		assertSuccessfulResponseSchema(openapiPath.getPost(), ManipulationResponse.T.getTypeSignature(), OpenapiType.OBJECT);
		assertSuccessfulResponseSchema(openapiPath.getPatch(), ManipulationResponse.T.getTypeSignature(), OpenapiType.OBJECT);
	}

	private OpenapiSchema assertSuccessfulResponseSchema(OpenapiOperation operation, String title, OpenapiType type) {
		OpenapiResponse response = getReferenced(operation.getResponses().get("200"));
		Assert.assertNotNull(response);

		OpenapiSchema responseSchema = getSchemaFromContent(response.getContent(), type != OpenapiType.OBJECT);
		assertSchema(responseSchema, title, type);

		return responseSchema;
	}

}
