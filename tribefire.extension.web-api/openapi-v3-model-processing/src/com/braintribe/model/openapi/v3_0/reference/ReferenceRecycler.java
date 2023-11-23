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
import com.braintribe.model.openapi.v3_0.OpenApi;
import com.braintribe.model.openapi.v3_0.reference.JsonReferenceOptimizer.OptimizationResult;
import com.braintribe.model.openapi.v3_0.reference.JsonReferenceOptimizer.ProcessingStatus;

/**
 * A {@link JsonReferenceBuilder} implementation that works for {@link ReferenceRecyclingContext}s which might have more
 * general parent contexts, which again are used by several more child contexts as parent context. Child contexts can
 * reuse entities built by their parent contexts if the child contexts don't produce different results (e.g. because
 * they resolve metadata differently).
 * <p>
 * The goal is to optimize for reusing a reference as often as possible, which again allows for smaller {@link OpenApi}
 * documents that are faster to load and process.
 * <p>
 * Note, that reference objects are created before the actual target entity is known and the target might be changed
 * multiple times before the final result. However by holding the reference object (in a java sense), you will
 * automatically point (with {@link JsonReferencable#get$ref()}) to the optimal target in the end (in a JSON-reference
 * sense).
 * <p>
 * Note also, that the algorithm would be easily adaptable for other kinds of late-binding references.
 *
 * @see JsonReferenceBuilder
 * @author Neidhart.Orlich
 *
 */
public abstract class ReferenceRecycler<T extends JsonReferencable, A> implements JsonReferenceBuilder<T, A> {

	private final EntityType<T> typeToBeBuilt; // type of WithRef - used to create a new raw ref instance.
	private final Map<String, T> entityPool;
	private final String refPrefix;
	private final JsonReferenceOptimizer<A> optimizer;
	protected final A ownContext;

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
	public ReferenceRecycler(EntityType<T> type, Map<String, T> entityPool, String poolReferenceKey, ReferenceRecyclingContext<A> context) {
		this.typeToBeBuilt = type;
		this.entityPool = entityPool;
		this.ownContext = context.publicApiContext();

		optimizer = context.getOptimizationResults();
		refPrefix = poolReferenceKey + "/";
	}

	/**
	 * If and only if no entity exists for this builder's reference key the factory gets called to create that very
	 * entity. Note that the factory will also be called by eventual parent contexts of this builder's context.
	 *
	 * @param factory
	 *            Entity factory that takes the builder's context as an argument
	 */
	@Override
	public ReferenceRecycler<T, A> ensure(Function<A, T> factory) {

		optimizer.getOptimized(factory, this);

		return this;
	}

	@Override
	public T getRef() {
		OptimizationResult cached = optimizer.getCached(getContextUnawareRefString());

		if (cached == null) {
			throw new IllegalStateException("No result found for '" + getContextUnawareRefString() + "'. Please call the ensure() method first.");
		}

		if (cached.getStatus() == ProcessingStatus.notPresent) {
			return null;
		}

		String ref = cached.getRefString();

		if (ref != null) {
			int lastSlash = ref.lastIndexOf("/");
			String componentKey = ref.substring(lastSlash + 1);

			if (!entityPool.containsKey(componentKey)) {
				throw new IllegalStateException("Component '" + ref + "' must be ensured before a reference can be used.");
			}
		}

		return (T) cached.getReference();
	}

	/* package-private */ abstract protected String getRefKey();

	/* package-private */ abstract protected boolean isValidInContext(ReferenceRecyclingContext<A> context);

	/* package-private */ String getContextUnawareRefString() {
		return refPrefix + getRefKey();
	}

	/* package-private */ String getContextAwareRefString(ReferenceRecyclingContext<A> context) {
		return refPrefix + getContextAwareRefKey(context);
	}

	protected String getContextAwareRefKey(ReferenceRecyclingContext<A> context) {
		return getRefKey() + context.getKeySuffix();
	}

	/* package-private */ T getRawRef() {
		// return typeToBeBuilt.create(getContextAwareRefString(ownContext)); // enable for debugging
		return typeToBeBuilt.create();
	}

	/* package-private */ void storeInPool(ReferenceRecyclingContext<A> creatorContext, T component) {
		String contextAwareKey = getContextAwareRefKey(creatorContext);
		entityPool.put(contextAwareKey, component);
	}

}
