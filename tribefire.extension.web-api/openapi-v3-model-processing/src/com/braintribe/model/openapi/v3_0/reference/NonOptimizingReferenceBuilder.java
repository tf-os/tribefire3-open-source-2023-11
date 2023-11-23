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

import java.util.Map;
import java.util.function.Function;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.openapi.v3_0.JsonReferencable;

/**
 * This simple implementation of a {@link JsonReferenceBuilder} stores a component into an entity pool and then provides
 * a reference to it without any further optimizations.
 *
 * @see JsonReferenceBuilder
 * @author Neidhart.Orlich
 *
 */
public abstract class NonOptimizingReferenceBuilder<T extends JsonReferencable, A> implements JsonReferenceBuilder<T, A> {

	private final Map<String, T> entityPool;
	private final String refPrefix;
	protected final A ownContext;
	private final T ref;

	/**
	 * @param type
	 *            Entity type of the entities this builder builds.
	 * @param entityPool
	 *            Pool where the actual entities get stored (not their references)
	 * @param poolReferenceKey
	 *            JSON Reference key to the entityPool
	 * @param context
	 *            context that should be passed to {@link #ensure(Function)} to build the actual entity
	 */
	public NonOptimizingReferenceBuilder(EntityType<T> type, Map<String, T> entityPool, String poolReferenceKey,
			ReferenceRecyclingContext<A> context) {
		this.ref = type.create();
		this.entityPool = entityPool;
		this.ownContext = context.publicApiContext();

		refPrefix = poolReferenceKey + "/";
	}

	@Override
	public NonOptimizingReferenceBuilder<T, A> ensure(Function<A, T> factory) {

		String componentKey = getRefKey();
		
		entityPool.computeIfAbsent(componentKey, k -> factory.apply(ownContext));
		
		ref.set$ref(refPrefix + componentKey);

		return this;
	}

	@Override
	public T getRef() {
		if (ref.get$ref() == null) {
			throw new IllegalStateException("Component '" + refPrefix + getRefKey() + "' must be ensured before a reference can be used.");
		}

		return ref;
	}

	public abstract String getRefKey();

}
