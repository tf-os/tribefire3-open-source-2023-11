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
package com.braintribe.model.processing.meta.oracle;

import java.util.Set;

import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.meta.GmBaseType;
import com.braintribe.model.meta.GmEntityType;
import com.braintribe.model.meta.GmType;

import jsinterop.annotations.JsType;

/**
 * Represents a type hierarchy rooted at a given {@link GmEntityType}.
 * 
 * @author peter.gazdik
 */
@JsType(namespace = GmCoreApiInteropNamespaces.model)
@SuppressWarnings("unusable-by-js")
public interface TypeHierarchy {

	TypeHierarchy transitive();

	/** Includes the base of this hierarchy as long as it matches the "instantiability" parameter. */
	TypeHierarchy includeSelf();

	/**
	 * Includes the base of this hierarchy regardless of the "instantiability" parameter.
	 * 
	 * @see #includeSelf()
	 */
	TypeHierarchy includeSelfForce();

	TypeHierarchy onlyInstantiable();

	TypeHierarchy onlyAbstract();

	/** Includes the base type. This is only relevant when we are retrieving {@link EntityTypeOracle#getSuperTypes() superTypes}. */
	TypeHierarchy includeBaseType();

	/** Specifies that the resulting set will sorted according to given {@link Order}. */
	TypeHierarchy sorted(Order order);

	public static enum Order {
		subFirst,
		superFirst
	}

	/**
	 * Returns an UNMODIFIABLE {@link Set} of {@link GmType}s based on given options.
	 * 
	 * The order in case of super-types is given in depth-first order, but is {@link GmBaseType} is included, that is always the last one.
	 * 
	 * If "self" is included, it is always the first (for both sub and super).
	 */
	<T extends GmType> Set<T> asGmTypes();

	/**
	 * Returns an UNMODIFIABLE {@link Set} of {@link GenericModelType}s based on given options.
	 * 
	 * Note that the order corresponds to the order of {@link #asGmTypes()} result.
	 */
	<T extends GenericModelType> Set<T> asTypes();

	/**
	 * Returns an UNMODIFIABLE {@link Set} of {@link EntityTypeOracle}s based on given options.
	 * 
	 * Note that the order corresponds to the order of {@link #asGmTypes()} result.
	 * 
	 * @throws IllegalStateException
	 *             if {@link #includeBaseType()} option was used, because this combination doesn't make sense.
	 */
	Set<EntityTypeOracle> asEntityTypeOracles();

}
