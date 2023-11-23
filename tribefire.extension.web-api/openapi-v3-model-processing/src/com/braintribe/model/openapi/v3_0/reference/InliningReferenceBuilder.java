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

import java.util.function.Function;

import com.braintribe.model.openapi.v3_0.JsonReferencable;

/**
 * This trivial implementation of a {@link JsonReferenceBuilder} always creates a new instance instead of storing it in
 * a pool and providing references to it.
 */
public class InliningReferenceBuilder<T extends JsonReferencable, A> implements JsonReferenceBuilder<T, A> {

	private final String key;
	private final A ownContext;
	private T builtEntity;

	public InliningReferenceBuilder(String key, ReferenceRecyclingContext<A> context) {
		this.key = key;
		this.ownContext = context.publicApiContext();
	}

	@Override
	public InliningReferenceBuilder<T, A> ensure(Function<A, T> factory) {
		builtEntity = factory.apply(ownContext);
		return this;
	}

	@Override
	public T getRef() {
		if (builtEntity == null) {
			throw new IllegalStateException("No result found for '" + key + "'. Please call the ensure() method first.");
		}

		return builtEntity;
	}

}
