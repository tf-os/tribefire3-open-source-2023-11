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

import com.braintribe.model.openapi.v3_0.OpenapiComponents;
import com.braintribe.model.openapi.v3_0.OpenapiParameter;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.model.openapi.v3_0.reference.utils.AbstractComponentsTest;
import com.braintribe.model.openapi.v3_0.reference.utils.TestApiContext;

public class SimpleComponentsTest extends AbstractComponentsTest {
	@Test
	public void testSimple() {

		// An OpenapiSchema of type OpenapiComponents which again references a schema of Type OpenapiParameter
		OpenapiSchema schemaRef = schemaRef(rootContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T);

			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);
			return comp;
		});

		OpenapiSchema schemaComponent = getSchemaComponent(OpenapiComponents.T, rootContext, schemaRef);

		OpenapiSchema parameterRef = schemaComponent.getItems();

		OpenapiSchema childComponent = getSchemaComponent(OpenapiParameter.T, rootContext, parameterRef);

		assertThat(childComponent.getDescription()).isEqualTo(OpenapiParameter.T.getTypeSignature());

	}

	@Test
	public void testMultipleContexts() {

		TestApiContext childContext = rootContext.childContext("CHILD");

		// An OpenapiSchema of type OpenapiComponents which again references a schema of Type OpenapiParameter
		OpenapiSchema schemaRef = schemaRef(childContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T);

			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);
			return comp;
		});

		// Because the child context doesn't introduce any changes the components should be registered under the root
		// context
		OpenapiSchema schemaComponent = getSchemaComponent(OpenapiComponents.T, rootContext, schemaRef);

		OpenapiSchema parameterRef = schemaComponent.getItems();

		OpenapiSchema childComponent = getSchemaComponent(OpenapiParameter.T, rootContext, parameterRef);

		assertThat(childComponent.getDescription()).isEqualTo(OpenapiParameter.T.getTypeSignature());
	}

	@Test
	public void testManyContexts() {

		TestApiContext childContext = rootContext.childContext("CHILD");
		TestApiContext grandchildContext = childContext.childContext("CHILD");
		TestApiContext greatgrandchildContext = grandchildContext.childContext("CHILD");
		TestApiContext greatgreatgrandchildContext = greatgrandchildContext.childContext("CHILD");

		// An OpenapiSchema of type OpenapiComponents which again references a schema of Type OpenapiParameter
		OpenapiSchema schemaRef = schemaRef(greatgreatgrandchildContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T);

			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);
			return comp;
		});

		// Because the child context doesn't introduce any changes the components should be registered under the root
		// context
		OpenapiSchema schemaComponent = getSchemaComponent(OpenapiComponents.T, rootContext, schemaRef);

		OpenapiSchema parameterRef = schemaComponent.getItems();

		OpenapiSchema childComponent = getSchemaComponent(OpenapiParameter.T, rootContext, parameterRef);

		assertThat(childComponent.getDescription()).isEqualTo(OpenapiParameter.T.getTypeSignature());
	}

	@Test
	public void testMultipleContextsWithChanges() {

		TestApiContext childContext = rootContext.childContext("CHILD");

		// An OpenapiSchema of type OpenapiComponents which again references a schema of Type OpenapiParameter
		OpenapiSchema schemaRef = schemaRef(childContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T);

			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);

			if (c == childContext) {
				comp.setDefault("Child context changes default");
			}

			return comp;
		});

		// Because the child context introduces changes to the OpenapiComponents schema it should be registered under
		// the child context
		OpenapiSchema schemaComponent = getSchemaComponent(OpenapiComponents.T, childContext, schemaRef);

		OpenapiSchema parameterRef = schemaComponent.getItems();

		// But no changes affecting the OpenapiParameter schema
		OpenapiSchema childComponent = getSchemaComponent(OpenapiParameter.T, rootContext, parameterRef);

		assertThat(childComponent.getDescription()).isEqualTo(OpenapiParameter.T.getTypeSignature());
	}

	@Test
	public void testManyContextsWithChanges() {

		TestApiContext childContext = rootContext.childContext("CHILD");
		TestApiContext grandchildContext = childContext.childContext("CHILD");
		TestApiContext greatgrandchildContext = grandchildContext.childContext("CHILD");
		TestApiContext greatgreatgrandchildContext = greatgrandchildContext.childContext("CHILD");

		// An OpenapiSchema of type OpenapiComponents which again references a schema of Type OpenapiParameter
		OpenapiSchema schemaRef = schemaRef(greatgreatgrandchildContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T);

			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);

			if (c == greatgrandchildContext) {
				comp.setDefault("Child context changes default");
			}

			return comp;
		});

		// Because the child context introduces changes to the OpenapiComponents schema it should be registered under
		// the child context
		OpenapiSchema schemaComponent = getSchemaComponent(OpenapiComponents.T, greatgrandchildContext, schemaRef);

		OpenapiSchema parameterRef = schemaComponent.getItems();

		// But no changes affecting the OpenapiParameter schema
		OpenapiSchema childComponent = getSchemaComponent(OpenapiParameter.T, rootContext, parameterRef);

		assertThat(childComponent.getDescription()).isEqualTo(OpenapiParameter.T.getTypeSignature());
	}

	@Test
	public void testMultipleContextsWithChanges2() {

		TestApiContext childContext = rootContext.childContext("CHILD");

		// An OpenapiSchema of type OpenapiComponents which again references a schema of Type OpenapiParameter
		OpenapiSchema schemaRef = schemaRef(childContext, OpenapiComponents.T, c -> {
			OpenapiSchema ref = schemaRef(c, OpenapiParameter.T, cc -> {
				OpenapiSchema cs = schemaFactory(OpenapiParameter.T).apply(cc);

				if (cc == childContext) {
					cs.setDefault("Child context changes default");
				}

				return cs;
			});

			OpenapiSchema comp = OpenapiSchema.T.create();
			comp.setItems(ref);

			return comp;
		});

		// Because the child context introduces changes to the OpenapiParameter schema which the OpenapiComponents
		// schema references,
		// also this schema needs to be registered under the child context
		OpenapiSchema schemaComponent = getSchemaComponent(OpenapiComponents.T, childContext, schemaRef);

		OpenapiSchema parameterRef = schemaComponent.getItems();

		// Because the child context introduces changes to the OpenapiParameter schema it should be registered under the
		// child context
		OpenapiSchema childComponent = getSchemaComponent(OpenapiParameter.T, childContext, parameterRef);

		assertThat(childComponent.getDescription()).isEqualTo(OpenapiParameter.T.getTypeSignature());
	}

	@Test
	public void testBuilder() {

		TestApiContext childContext = rootContext.childContext("CHILD");

		ComponentTestBuilder builder = new ComponentTestBuilder(this);
		builder.add(OpenapiParameter.T).changeForContext(childContext);

		// An OpenapiSchema of type OpenapiComponents which again references a schema of Type OpenapiParameter
		OpenapiSchema schemaRef = builder.buildRef(childContext);

		// Because the child context introduces changes to the OpenapiParameter schema which the OpenapiComponents
		// schema references,
		// also this schema needs to be registered under the child context
		OpenapiSchema schemaComponent = getSchemaComponent(OpenapiComponents.T, childContext, schemaRef);

		OpenapiSchema parameterRef = schemaComponent.getItems();

		// Because the child context introduces changes to the OpenapiParameter schema it should be registered under the
		// child context
		OpenapiSchema childComponent = getSchemaComponent(OpenapiParameter.T, childContext, parameterRef);

		assertThat(childComponent.getDescription()).isEqualTo(OpenapiParameter.T.getTypeSignature());
	}

}
