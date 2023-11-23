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

import java.util.HashMap;
import java.util.Map;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.model.openapi.v3_0.reference.ReferenceRecycler;
import com.braintribe.model.openapi.v3_0.reference.ReferenceRecyclingContext;
import com.braintribe.model.openapi.v3_0.reference.model.ComplexReferencable;
import com.braintribe.model.openapi.v3_0.reference.model.SimpleReferencable;

public class TestComponents {
	public final Map<String, OpenapiSchema> schemaComponents;
	public final Map<String, ComplexReferencable> complexComponents;
	public final Map<String, SimpleReferencable> simpleComponents;
//	private final OptimizingJsonReferenceContext<TestApiContext> optimizingContext;
	
	public TestComponents(Map<String, OpenapiSchema> schemaComponents, Map<String, ComplexReferencable> complexComponents,
			Map<String, SimpleReferencable> simpleComponents) {
		super();
		this.schemaComponents = schemaComponents;
		this.complexComponents = complexComponents;
		this.simpleComponents = simpleComponents;
//		this.optimizingContext = optimizingContext;
	}
	
	public TestComponents() {
		this(new HashMap<>(), new HashMap<>(), new HashMap<>());
	}

//	public TestComponents(OptimizingJsonReferenceContext<TestApiContext> optimizingContext, TestComponents testComponents) {
//		this(testComponents.schemaComponents, testComponents.complexComponents, testComponents.simpleComponents, optimizingContext);
//	}

	private final class SchemaBuilder extends ReferenceRecycler<OpenapiSchema, TestApiContext> {
		private final String typeSignature;
		private final EntityType<?> respectiveType;

		public SchemaBuilder(EntityType<?> type, ReferenceRecyclingContext<TestApiContext> context) {
			super(OpenapiSchema.T, schemaComponents, "#/components/schemas", context);
			
			this.respectiveType = type;
			typeSignature = respectiveType.getTypeSignature();
		}
		
		@Override
		protected String getRefKey() {
			return typeSignature;
		}

		@Override
		protected boolean isValidInContext(ReferenceRecyclingContext<TestApiContext> context) {
			return context.publicApiContext().supportsType(respectiveType);
		}
	}
	
	private final class ComplexReferencableBuilder extends ReferenceRecycler<ComplexReferencable, TestApiContext> {
		private final String refKey;
		
		public ComplexReferencableBuilder(String refKey, ReferenceRecyclingContext<TestApiContext> context) {
			super(ComplexReferencable.T, complexComponents, "#/components/complex", context);
			
			this.refKey = refKey;
		}
		
		@Override
		protected String getRefKey() {
			return refKey;
		}

		@Override
		protected boolean isValidInContext(ReferenceRecyclingContext<TestApiContext> context) {
			return true;
		}
	}
	
	private final class SimpleReferencableBuilder extends ReferenceRecycler<SimpleReferencable, TestApiContext> {
		private final String refKey;
		
		public SimpleReferencableBuilder(String refKey, ReferenceRecyclingContext<TestApiContext> context) {
			super(SimpleReferencable.T, simpleComponents, "#/components/simple", context);
			
			this.refKey = refKey;
		}
		
		@Override
		protected String getRefKey() {
			return refKey;
		}

		@Override
		protected boolean isValidInContext(ReferenceRecyclingContext<TestApiContext> context) {
			return true;
		}
	}

	public ReferenceRecycler<OpenapiSchema, TestApiContext> schema(EntityType<?> schema, ReferenceRecyclingContext<TestApiContext> optimizingContext) {
		return new SchemaBuilder(schema, optimizingContext);
	}
	
	public ReferenceRecycler<ComplexReferencable, TestApiContext> complexReferencable(String refKey, ReferenceRecyclingContext<TestApiContext> optimizingContext) {
		return new ComplexReferencableBuilder(refKey, optimizingContext);
	}
	
	public ReferenceRecycler<SimpleReferencable, TestApiContext> simpleReferencable(String refKey, ReferenceRecyclingContext<TestApiContext> optimizingContext) {
		return new SimpleReferencableBuilder(refKey, optimizingContext);
	}
}
