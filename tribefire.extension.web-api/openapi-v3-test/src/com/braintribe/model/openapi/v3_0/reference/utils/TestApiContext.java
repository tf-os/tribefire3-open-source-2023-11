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

import java.util.function.Predicate;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.openapi.v3_0.OpenapiSchema;
import com.braintribe.model.openapi.v3_0.reference.ReferenceRecycler;
import com.braintribe.model.openapi.v3_0.reference.ReferenceRecyclingContext;
import com.braintribe.model.openapi.v3_0.reference.model.ComplexReferencable;
import com.braintribe.model.openapi.v3_0.reference.model.SimpleReferencable;

public class TestApiContext {

	private final Predicate<GenericModelType> supportsType;

	private final TestComponents testComponents;
	private final ReferenceRecyclingContext<TestApiContext> optimizingContext;

	private TestApiContext(ReferenceRecyclingContext<TestApiContext> testJsonReferencingContext, TestComponents testComponents) {
		this(testJsonReferencingContext, testComponents, t -> true);
	}

	private TestApiContext(ReferenceRecyclingContext<TestApiContext> testJsonReferencingContext, TestComponents testComponents,
			Predicate<GenericModelType> supportsType) {
		this.supportsType = supportsType;
		this.testComponents = testComponents;
		this.optimizingContext = testJsonReferencingContext;
	}

	public boolean supportsType(GenericModelType type) {
		return supportsType.test(type);
	}

	public ReferenceRecycler<OpenapiSchema, TestApiContext> schema(EntityType<?> schema) {
		return testComponents.schema(schema, optimizingContext);
	}

	public ReferenceRecycler<ComplexReferencable, TestApiContext> complexReferencable(String refKey) {
		return testComponents.complexReferencable(refKey, optimizingContext);
	}

	public ReferenceRecycler<SimpleReferencable, TestApiContext> simpleReferencable(String refKey) {
		return testComponents.simpleReferencable(refKey, optimizingContext);
	}

	public TestApiContext childContext(String string) {
		return childContext(string, supportsType);
	}

	public TestApiContext childContext(String string, Predicate<GenericModelType> supportsType) {
		return optimizingContext.childContext(string, o -> factory(o, testComponents, supportsType)).publicApiContext();
	}

	public static TestApiContext create(String key, Predicate<GenericModelType> supportsType) {
		return new ReferenceRecyclingContext<TestApiContext>(null, key, o -> factory(o, new TestComponents(), supportsType)).publicApiContext();
	}

	public static TestApiContext create(String key, TestComponents parentComponents, Predicate<GenericModelType> supportsType) {
		return new ReferenceRecyclingContext<TestApiContext>(null, key, o -> factory(o, parentComponents, supportsType)).publicApiContext();
	}

	private static TestApiContext factory(ReferenceRecyclingContext<TestApiContext> optimizingContext, TestComponents parentComponents,
			Predicate<GenericModelType> supportsType) {
		return new TestApiContext(optimizingContext, parentComponents, supportsType);
	}

	public void seal() {
		optimizingContext.seal();
	}

	public String contextDescription() {
		return "TestJsonReferencingContext '" + optimizingContext.getKeySuffix() + "'";
	}

	@Override
	public String toString() {
		return contextDescription();
	}

	public TestComponents components() {
		return testComponents;
	}

	public String getKeySuffix() {
		return optimizingContext.getKeySuffix();
	}
}
