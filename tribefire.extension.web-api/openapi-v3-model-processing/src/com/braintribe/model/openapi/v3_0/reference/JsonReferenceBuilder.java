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
 * A JsonReferenceBuilder helps building a JSON reference (https://json-spec.readthedocs.io/reference.html) which is
 * modeled via deriving from the {@link JsonReferencable} entity.
 * <p>
 * A JSON reference object holds a String key describing a path how to get to the respective entity. The actual entities are
 * stored in a pool (a map from a String ID to the entity) which also must be referencable by a known JSON reference
 * key. That means that the JSON reference key will consist of the JSON reference key of the pool plus its own unique
 * key.
 * <p>
 * This builder's task is to
 * <ol>
 * <li>Determine the JSON reference key for a specific entity
 * <li>Ensure that an entity exists with that key in the pool.
 * <li>If not, create it in the current JsonReferencingContext and store it in the pool. ( {@link #ensure(Function)} )
 * <li>Return a reference object in the correct type with that key ( {@link #getRef()} )
 * </ol>
 *
 * @author Neidhart.Orlich
 * @see ReferenceRecycler
 */
public interface JsonReferenceBuilder<T extends JsonReferencable, A> {

	/**
	 * The builder checks if there already is a suitable entity in the pool. What suitable means depends on the
	 * builder's implementation. Only if no suitable entity is found, the factory gets called to create that very
	 * entity. Note that the factory might be called multiple times with different contexts. E.g. there might be several
	 * possible ways to create the entity and to find the best one, different versions need to be compared.
	 *
	 * @param factory
	 *            Entity factory that takes a context object as an argument. The type of context depends on the builder
	 *            implementation. The factory must always return identical components for the same context.
	 * @return itself
	 */
	JsonReferenceBuilder<T, A> ensure(Function<A, T> factory);

	/**
	 * Returns an entity of the correct type but with no property set except the {@link JsonReferencable#get$ref()} one
	 * which points to the actual entity in the pool.
	 */
	T getRef();

}