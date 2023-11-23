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

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.openapi.v3_0.OpenapiComponents;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.model.openapi.v3_0.reference.utils.AbstractComponentsTest;
import com.braintribe.model.openapi.v3_0.reference.utils.TestApiContext;

public class ComponentTestBuilder { // TODO: extend from common superclass
	private static class SchemaNode {
		final EntityType<?> schemaType;
		final SchemaNode parentNode;
		final Map<TestApiContext, String> changesForContexts = new HashMap<>();

		boolean alreadyExisting;

		SchemaNode itemNode;
		SchemaNode addPropNode;

		public SchemaNode(EntityType<?> schemaType, SchemaNode parentNode) {
			this.schemaType = schemaType;
			this.parentNode = parentNode;
		}

		public OpenapiSchema buildRef(TestApiContext context) {

			return context.schema(schemaType).ensure(c -> {
				if (alreadyExisting) {
					throw new IllegalStateException("Component was expected to already exist but factory was called");
				}

				OpenapiSchema schemaComponent = OpenapiSchema.T.create();
				schemaComponent.setDescription(schemaType.getTypeSignature());

				if (itemNode != null) {
					OpenapiSchema itemNodeRef = itemNode.buildRef(c);
					schemaComponent.setItems(itemNodeRef);
				}

				if (addPropNode != null) {
					OpenapiSchema addPropRef = addPropNode.buildRef(c);
					schemaComponent.setAdditionalProperties(addPropRef);
				}

				if (changesForContexts.containsKey(c)) {
					String change = changesForContexts.get(c);
					schemaComponent.setDefault("Changed for context " + change);
				}

				return schemaComponent;
			}).getRef();
		}

	}

	private final SchemaNode currentNode;
	private final AbstractComponentsTest test;

	private ComponentTestBuilder(SchemaNode rootNode, AbstractComponentsTest test) {
		this.currentNode = rootNode;
		this.test = test;
	}

	ComponentTestBuilder(AbstractComponentsTest test) {
		this(new SchemaNode(OpenapiComponents.T, null), test);
	}

	public ComponentTestBuilder addAlreadyExisting(EntityType<?> schemaType) {
		ComponentTestBuilder added = add(schemaType);
		added.currentNode.alreadyExisting = true;

		return added;
	}

	public ComponentTestBuilder add(EntityType<?> schemaType) {
		SchemaNode childNode = new SchemaNode(schemaType, currentNode);

		currentNode.itemNode = childNode;
		return new ComponentTestBuilder(childNode, test);
	}

	public ComponentTestBuilder addBranch(EntityType<?> schemaType) {
		SchemaNode childNode = new SchemaNode(schemaType, currentNode);

		currentNode.addPropNode = childNode;
		return new ComponentTestBuilder(childNode, test);
	}

	public ComponentTestBuilder changeForContexts(String change, TestApiContext... contexts) {
		Stream.of(contexts).forEach(c -> currentNode.changesForContexts.put(c, change));
		return this;
	}

	public ComponentTestBuilder changeForContext(TestApiContext context) {
		currentNode.changesForContexts.put(context, context.getKeySuffix());

		return this;
	}

	public OpenapiSchema buildRef(TestApiContext context) {
		return currentNode.buildRef(context);
	}

}
