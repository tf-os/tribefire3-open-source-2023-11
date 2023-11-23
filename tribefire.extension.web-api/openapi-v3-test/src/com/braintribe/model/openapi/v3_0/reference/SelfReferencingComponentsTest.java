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
package com.braintribe.model.openapi.v3_0.reference;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.openapi.v3_0.OpenapiParameter;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.model.openapi.v3_0.reference.utils.AbstractComponentsTest;
import com.braintribe.model.openapi.v3_0.reference.utils.TestApiContext;

public class SelfReferencingComponentsTest extends AbstractComponentsTest {
	@Test
	public void testSimple() {

		// An OpenapiSchema of type OpenapiParameter which again references a schema of Type OpenapiParameter
		// Both schemas should be the exact same instance -> a self-reference
		OpenapiSchema schemaRef = schemaRef(rootContext, OpenapiParameter.T, c -> {
			OpenapiSchema ref = alreadyPresentSchemaRef(c, OpenapiParameter.T);

			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);
			return comp;
		});

		OpenapiSchema schemaComponent = getSchemaComponent(OpenapiParameter.T, rootContext, schemaRef);

		OpenapiSchema parameterRef = schemaComponent.getItems();

		OpenapiSchema childComponent = getSchemaComponent(OpenapiParameter.T, rootContext, parameterRef);

		assertThat(childComponent).isSameAs(schemaComponent);

	}

	@Test
	public void testMultipleContexts() {

		TestApiContext childContext = rootContext.childContext("CHILD");

		OpenapiSchema schemaRef = schemaRef(childContext, OpenapiParameter.T, c -> {
			OpenapiSchema ref = alreadyPresentSchemaRef(c, OpenapiParameter.T);

			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);
			return comp;
		});

		// Because the child context doesn't introduce any changes the components should be registered under the root
		// context
		OpenapiSchema schemaComponent = getSchemaComponent(OpenapiParameter.T, rootContext, schemaRef);

		OpenapiSchema parameterRef = schemaComponent.getItems();

		OpenapiSchema childComponent = getSchemaComponent(OpenapiParameter.T, rootContext, parameterRef);

		assertThat(childComponent).isSameAs(schemaComponent);
	}

	@Test
	public void testMultipleContextsWithChanges() {

		TestApiContext childContext = rootContext.childContext("CHILD");

		OpenapiSchema schemaRef = schemaRef(childContext, OpenapiParameter.T, c -> {
			OpenapiSchema ref = alreadyPresentSchemaRef(c, OpenapiParameter.T);

			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);

			if (c == childContext) {
				comp.setDefault("Child context changes default");
			}

			return comp;
		});

		// Because the child context introduces changes to the OpenapiComponents schema it should be registered under
		// the child context
		OpenapiSchema schemaComponent = getSchemaComponent(OpenapiParameter.T, childContext, schemaRef);

		OpenapiSchema parameterRef = schemaComponent.getItems();

		// Because child and parent component are identical this should return the same
		OpenapiSchema childComponent = getSchemaComponent(OpenapiParameter.T, childContext, parameterRef);

		assertThat(childComponent).isSameAs(schemaComponent);

	}

}
