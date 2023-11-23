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
package com.braintribe.model.openapi.v3_0.reference.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Set;
import java.util.function.Function;

import org.junit.Before;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.openapi.v3_0.JsonReferencable;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.utils.CollectionTools;

public abstract class AbstractComponentsTest {
	protected TestComponents testComponents;
	protected TestApiContext rootContext;

	protected final Set<GenericModelType> rootContextSupportsTypes = CollectionTools.getSet(OpenapiSchema.T);

	@Before
	public void init() {
		rootContext = TestApiContext.create("ROOT", AbstractComponentsTest::isOpenapiEntity);
		testComponents = rootContext.components();
	}

	public static boolean isOpenapiEntity(GenericModelType type) {
		return type.getTypeSignature().contains("Openapi");
	}

	protected OpenapiSchema getSchemaComponent(EntityType<?> type, TestApiContext context, JsonReferencable ref) {
		String prefix = "#/components/schemas/";
		String refString = ref.get$ref();

		assertThat(refString).startsWith(prefix);

		String refKey = refString.substring(prefix.length());

		String keySuffix = context.getKeySuffix();
		assertThat(refKey).endsWith(keySuffix);

		String typePartOfKey = refKey.substring(0, refKey.length() - keySuffix.length());
		assertThat(typePartOfKey).isEqualTo(type.getTypeSignature());

		OpenapiSchema openapiSchema = testComponents.schemaComponents.get(refKey);

		assertThat(openapiSchema).isNotNull();

		return openapiSchema;
	}

	protected OpenapiSchema getSchemaComponentFromRef(JsonReferencable ref) {
		String prefix = "#/components/schemas/";
		String refKey = ref.get$ref().substring(prefix.length());

		return testComponents.schemaComponents.get(refKey);
	}

	private static void assertIsReference(JsonReferencable ref) {

		EntityType<GenericEntity> entityType = ref.entityType();

		for (Property property : entityType.getProperties()) {
			if (property.getName().equals("$ref")) {
				assertThat(!property.isEmptyValue(ref));
			} else {
				assertThat(property.isEmptyValue(ref));
			}
		}
	}

	protected static OpenapiSchema schemaRef(TestApiContext context, EntityType<?> type, Function<TestApiContext, OpenapiSchema> factory) {
		OpenapiSchema ref = context.schema(type).ensure(factory).getRef();

		if (ref == null) {
			// schema not present in context
			return null;
		}

		assertIsReference(ref);

		return ref;
	}

	protected static OpenapiSchema alreadyPresentSchemaRef(TestApiContext context, EntityType<?> type) {
		return schemaRef(context, type, c -> {
			fail("Factory should not have been called because schema is already expected to be present.");
			return null;
		});
	}

	protected static OpenapiSchema schemaRef(TestApiContext context, EntityType<?> type) {
		return schemaRef(context, type, schemaFactory(type));
	}

	protected static Function<TestApiContext, OpenapiSchema> schemaFactory(EntityType<?> type) {
		return c -> {
			OpenapiSchema schema = OpenapiSchema.T.create();
			schema.setDescription(type.getTypeSignature());
			return schema;
		};
	}

}