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

import static com.braintribe.model.openapi.v3_0.export.AbstractOpenapiProcessor.ALL_MEDIA_TYPES_RANGE;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.openapi.v3_0.JsonReferencable;
import com.braintribe.model.openapi.v3_0.OpenApi;
import com.braintribe.model.openapi.v3_0.OpenapiComponents;
import com.braintribe.model.openapi.v3_0.OpenapiMediaType;
import com.braintribe.model.openapi.v3_0.OpenapiOperation;
import com.braintribe.model.openapi.v3_0.OpenapiParameter;
import com.braintribe.model.openapi.v3_0.OpenapiPath;
import com.braintribe.model.openapi.v3_0.OpenapiRequestBody;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.model.openapi.v3_0.OpenapiType;
import com.braintribe.model.openapi.v3_0.export.legacytests.wire.contract.AbstractOpenApiTestContract;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.utils.lcd.CollectionTools;
import com.braintribe.wire.api.Wire;
import com.braintribe.wire.api.context.WireContext;
import com.braintribe.wire.api.module.WireTerminalModule;

public abstract class AbstractOpenapiProcessorTest<S extends AbstractOpenApiTestContract> {

	protected static OpenapiComponents components = null;

	public static final String URLENCODED = "application/x-www-form-urlencoded";
	public static final String MULTIPART_FORM_DATA = "multipart/form-data";
	public static final String APPLICATION_JSON = "application/json";

	protected abstract WireTerminalModule<S> module();

	protected WireContext<S> wireContext;
	protected S contract;
	protected Evaluator<ServiceRequest> evaluator;

	@Before
	public void initTest() {
		components = null;
		wireContext = Wire.context(module());
		contract = wireContext.contract();
		evaluator = contract.evaluator();
	}

	@After
	public void closeTest() {
		wireContext.shutdown();
	}

	protected void assertIdAndPartition(OpenapiOperation operation, boolean withPathId, boolean withPathPartition) {
		if (withPathId) {
			assertSimpleParameter(operation, "id", "path", "string", true, null);
		}
		if (withPathPartition) {
			assertSimpleParameter(operation, "partition", "path", "string", true, null);
		}
	}

	protected OpenapiPath getPath(OpenApi api, String apiPath) {
		Map<String, OpenapiPath> paths = api.getPaths();
		OpenapiPath path = paths.get(apiPath);
		Assert.assertNotNull(path);

		return path;
	}

	protected void assertEndpointParameters(OpenapiOperation operation) {
		assertSimpleParameter(operation, "endpoint.depth", "query", "string", false, "3");
		assertSimpleParameter(operation, "endpoint.prettiness", "query", "string", false, "mid");
		assertSimpleParameter(operation, "endpoint.stabilizeOrder", "query", "boolean", false, false);
		assertSimpleParameter(operation, "endpoint.writeEmptyProperties", "query", "boolean", false, false);
		assertSimpleParameter(operation, "endpoint.typeExplicitness", "query", "string", false, "auto");
		assertSimpleParameter(operation, "endpoint.entityRecurrenceDepth", "query", "integer", false, 0);
		assertSimpleParameter(operation, "endpoint.identityManagementMode", "query", "string", false, "auto");
	}

	protected void assertDefaultSetEndpointParameters(OpenapiOperation operation) {
		assertSimpleParameter(operation, "endpoint.depth", "query", "string", false, 0);
		assertSimpleParameter(operation, "endpoint.prettiness", "query", "string", false, "high");
		assertSimpleParameter(operation, "endpoint.stabilizeOrder", "query", "boolean", false, true);
		assertSimpleParameter(operation, "endpoint.writeEmptyProperties", "query", "boolean", false, true);
		assertSimpleParameter(operation, "endpoint.typeExplicitness", "query", "string", false, "always");
		assertSimpleParameter(operation, "endpoint.entityRecurrenceDepth", "query", "integer", false, 1);
		assertSimpleParameter(operation, "endpoint.identityManagementMode", "query", "string", false, "auto");
	}

	protected void assertSimpleParameter(OpenapiOperation operation, String name, String in, String type, boolean required, Object defaultValue) {
		OpenapiParameter parameter = getParameter(operation, name, in);
		OpenapiSchema schema = parameter.getSchema();
		Assert.assertNotNull(parameter);
		Assert.assertNotNull(schema);

		Assert.assertEquals(OpenapiType.parse(type), schema.getType());
		assertThat(required).as("parameter " + name + "'s required status should be " + required + " but is not.").isEqualTo(parameter.getRequired());
		Assert.assertEquals(defaultValue, schema.getDefault());
	}

	protected OpenapiSchema getBodyParameter(OpenapiOperation operation, String name) {
		OpenapiRequestBody body = getReferenced(operation.getRequestBody());
		Assert.assertTrue(body.getRequired());

		assertThat(body.getContent()).containsOnlyKeys(APPLICATION_JSON, ALL_MEDIA_TYPES_RANGE);
		OpenapiSchema schema = body.getContent().get(ALL_MEDIA_TYPES_RANGE).getSchema();

		return schema;
	}

	protected OpenapiParameter getParameter(OpenapiOperation operation, String name, String in) {
		for (OpenapiParameter parameterReference : operation.getParameters()) {
			OpenapiParameter parameter = getReferenced(parameterReference);
			if (in.equals(parameter.getIn()) && name.equals(parameter.getName())) {
				return parameter;
			}
		}

		return null;
	}

	protected void assertSchemaProperty(OpenapiSchema schema, String name, String title, OpenapiType type, boolean required) {
		OpenapiSchema propertySchema = schema.getProperties().get(name);

		Assert.assertEquals(title, propertySchema.getTitle());
		Assert.assertEquals(type, propertySchema.getType());
		Assert.assertEquals(required, schema.getRequired().contains(name));
	}

	protected static <T extends JsonReferencable> T getReferenced(T withRef) {
		Assert.assertNotNull(withRef);
		Assert.assertNotNull(withRef.get$ref());
		String[] splitRefPath = withRef.get$ref().replace("#/components/", "").split("/");

		Assertions.assertThat(splitRefPath).hasSize(2);

		Map<String, T> subcomponents = OpenapiComponents.T.findProperty(splitRefPath[0]).get(components);

		T result = subcomponents.get(splitRefPath[1]);
		Assert.assertNotNull(result);
		return result;
	}

	protected void assertSchema(OpenapiSchema schema, String title, OpenapiType type) {
		Assert.assertEquals(title, schema.getTitle());
		Assert.assertEquals(type, schema.getType());
	}

	protected void assertEnumeration(OpenapiSchema schema, String... values) {
		Assert.assertEquals("string", schema.getType());

		Set<String> expected = CollectionTools.getSet(values);
		Set<String> actual = new HashSet<>(schema.getEnum());
		Assert.assertEquals(expected, actual);
	}

	protected Map<String, OpenapiSchema> getSchemasFromContent(Map<String, OpenapiMediaType> content) {
		Map<String, OpenapiSchema> schemas = new HashMap<>();
		content.forEach((k, v) -> schemas.put(k, getReferenced(v.getSchema())));
		return schemas;
	}

	protected OpenapiSchema getSchemaFromContent(Map<String, OpenapiMediaType> content) {
		return getSchemaFromContent(content, false);
	}

	protected OpenapiSchema getSchemaFromContent(Map<String, OpenapiMediaType> content, boolean isSimpleType) {
		String JSON = "*/*";
		Assertions.assertThat(content).hasSize(2).containsKeys(JSON, ALL_MEDIA_TYPES_RANGE);
		OpenapiMediaType mediaType = content.get(JSON);
		OpenapiSchema schema = mediaType.getSchema();

		if (!isSimpleType) {
			schema = getReferenced(schema);
		}

		return schema;
	}

	private static OpenapiSchema unwrap(OpenapiSchema schema, Map<OpenapiSchema, OpenapiSchema> alreadyUnwapped) {
		if (schema == null) {
			return null;
		}

		if (alreadyUnwapped.containsKey(schema)) {
			return alreadyUnwapped.get(schema);
		}

		if ((schema.getType() != null && schema.getType() != OpenapiType.OBJECT && schema.getType() != OpenapiType.ARRAY)
				|| (schema.getTitle() != null && (schema.getTitle().equals("map") || schema.getTitle().equals("object")))) {
			return schema;
		}

		OpenapiSchema referenced = schema.getType() == OpenapiType.ARRAY ? schema : getReferenced(schema);

		alreadyUnwapped.put(schema, referenced);

		referenced.setItems(unwrap(referenced.getItems(), alreadyUnwapped));

		Map<String, OpenapiSchema> properties = new HashMap<>();

		referenced.getProperties().forEach((k, v) -> properties.put(k, unwrap(v, alreadyUnwapped)));
		referenced.setProperties(properties);

		return referenced;
	}

	protected static OpenapiSchema unwrap(OpenapiSchema schema) {
		// This seems to make a transitive deep clone
		// OpenapiSchema cloned = referenced.clone(new StandardCloningContext());

		return unwrap(schema, new HashMap<OpenapiSchema, OpenapiSchema>());
	}

	// protected static <R extends WithRef> R unwrap(R withRef){
	//
	// }

}
