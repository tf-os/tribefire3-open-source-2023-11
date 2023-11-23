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
package com.braintribe.model.generic.reflection;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.base.CollectionBase;

import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
public interface CollectionType extends EnhancableCustomType, EssentialCollectionTypes {

	@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
	enum CollectionKind {
		map,
		set,
		list
	}

	/**
	 * {@inheritDoc}
	 * 
	 * ATTENTION: The implementation actually does the check: {@code this == typeReflection.getType(value)}, which means
	 * two things:
	 * <ul>
	 * <li>If given collection doesn't implement {@link CollectionBase}, this method returns <tt>true</tt> iff
	 * <tt>this</tt> collection's parameterization consists of only Objects.</li>
	 * <li>If given collection does implement {@link CollectionBase}, this method returns <tt>true</tt> iff the types
	 * match exactly, so e.g. a set of {@link GenericEntity} is not an instance of set of Objects.</li>
	 * </ul>
	 * 
	 * @see CollectionBase
	 */
	@Override
	boolean isInstance(Object value);

	/**
	 * @returns <tt>true</tt> iff given collections may ONLY contain simple or enum types. In other words, in case of a
	 *          map this is true if both key and value are either simple or enum.
	 * 
	 * @see GenericModelType#areCustomInstancesReachable()
	 * @see GenericModelType#areEntitiesReachable()
	 */
	boolean hasSimpleOrEnumContent();

	CollectionKind getCollectionKind();

	/**
	 * Returns:
	 * <ul>
	 * <li>Element type - if <tt>List</tt> or <tt>Set</tt></li>
	 * <li>Value type - if <tt>Map</tt></li>
	 * </ul>
	 * 
	 * @see MapType#getValueType()
	 * @see MapType#getKeyType()
	 */
	GenericModelType getCollectionElementType();

	/**
	 * Static methods for generating type signature
	 */
	class TypeSignature {
		public static String forList(String elementSignature) {
			return "list<" + elementSignature + '>';
		}

		public static String forSet(String elementSignature) {
			return "set<" + elementSignature + '>';
		}

		public static String forMap(String keySignature, String valueSignature) {
			return "map<" + keySignature + ',' + valueSignature + '>';
		}
	}
}
