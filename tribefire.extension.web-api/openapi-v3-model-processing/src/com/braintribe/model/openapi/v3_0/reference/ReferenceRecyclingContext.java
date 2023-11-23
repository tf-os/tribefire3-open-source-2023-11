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
import java.util.function.Function;

public class ReferenceRecyclingContext<A> {

	private final Map<String, ReferenceRecyclingContext<A>> knownChildContexts = new HashMap<>();

	private final JsonReferenceOptimizer<A> jsonReferenceOptimizer;
	private final ReferenceRecyclingContext<A> parentContext;
	private final String keySuffix;
	private final A publicApiContext;
	private final Function<ReferenceRecyclingContext<A>, A> publicApiContextFactory;
	private boolean sealed;

	public ReferenceRecyclingContext(ReferenceRecyclingContext<A> parentContext, String keySuffix,
			Function<ReferenceRecyclingContext<A>, A> publicApiContextFactory) {
		this.parentContext = parentContext;
		this.publicApiContextFactory = publicApiContextFactory;
		this.keySuffix = normalizeKeySuffix(keySuffix);

		this.jsonReferenceOptimizer = new JsonReferenceOptimizer<>(this);
		this.publicApiContext = publicApiContextFactory.apply(this);
	}
	
	private static String normalizeKeySuffix(String keySuffix) {
		if(keySuffix == null)
			return "";

		if (keySuffix.startsWith("-"))
			return keySuffix;
		
		return "-" + keySuffix;
	}

	public JsonReferenceOptimizer<A> getOptimizationResults() {
		return jsonReferenceOptimizer;
	}

	public ReferenceRecyclingContext<A> getParentContext() {
		return parentContext;
	}

	public String getKeySuffix() {
		return keySuffix;
	}

	public String contextDescription() {
		return publicApiContext.toString();
	}

	public A publicApiContext() {
		return publicApiContext;
	}

	public ReferenceRecyclingContext<A> childContext(String keySuffix, Function<ReferenceRecyclingContext<A>, A> publicApiContextFactory) {
		if (sealed) {
			return new ReferenceRecyclingContext<>(this, keySuffix, publicApiContextFactory);
		}

		return knownChildContexts.computeIfAbsent(keySuffix, k -> new ReferenceRecyclingContext<>(this, keySuffix, publicApiContextFactory));
	}

	public ReferenceRecyclingContext<A> childContext(String key) {
		return childContext(key, publicApiContextFactory);
	}

	public void seal() {
		sealed = true;
		jsonReferenceOptimizer.seal();
	}
	
	public boolean isSealed() {
		return sealed;
	}
	
	@Override
	public String toString() {
		return (sealed ? "sealed " : "mutable ") + contextDescription();
	}
}
