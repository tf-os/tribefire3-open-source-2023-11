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
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.pr.criteria.matching.Matcher;
import com.braintribe.model.generic.value.ValueDescriptor;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * Base interface for all reflection classes which represent types of GM values. Every instance implementing this is
 * actually an instance of one of five "terminal" interfaces, which are listed in the "see also" section below.
 * 
 * NOTE that {@link CollectionType} is not really a terminal type, but we do not have to distinguish the concrete
 * sub-types most of the time.
 * 
 * @see BaseType
 * @see SimpleType
 * @see EntityType
 * @see EnumType
 * @see CollectionType
 */
@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
@SuppressWarnings("unusable-by-js")
public interface GenericModelType extends Comparable<GenericModelType> {

	/**
	 * Returns the java equivalent of given type.
	 * <p>
	 * For entities the interface class (whose name is the {@link #getTypeSignature() type signature} of this type).
	 * <p>
	 * For simple types where also primitive counterpart exists, this returns the object version. To retrieve the
	 * primitive class, call {@link SimpleType#getPrimitiveJavaType()}.
	 * <p>
	 * For collections, the returned value is one of three: {@code List.class},{@code Set.class} or {@code Map.class}.
	 */
	Class<?> getJavaType();

	/**
	 * This returns the type code for efficient usage in switch statements. The object type and custom types like
	 * CollectionType, EntityType, EnumType each have invariant type codes over all their custom instances.
	 * 
	 * @return the {@link TypeCode} for the given type
	 */
	TypeCode getTypeCode();

	/**
	 * Name for given type. The value depends on actual type in the following way:
	 * <ul>
	 * <li>BaseType: "object"</li>
	 * <li>SimpleType: one of: "boolean", "date", "decimal", "double", "float", "integer", "long", "string"</li>
	 * <li>CollectionType: one of: "list", "set", "map"</li>
	 * <li>EntityType: fully qualified name of the declaration interface (the one returned by {@link #getJavaType()}
	 * method)</li>
	 * <li>EnumType: fully qualified name of the enum class (the one returned by {@link #getJavaType()} method)</li>
	 * </ul>
	 */
	String getTypeName();

	/**
	 * Similar to {@link #getTypeName()}, but also includes the signatures of parameters in case of collections. An
	 * example for map would be: {@code "map<string,foo.Bar>"}.
	 */
	String getTypeSignature();

	/**
	 * Returns a string for given value, which depends on actual type in the following way:
	 * <ul>
	 * <li>SimpleType: natural representation of given value as String (i.e. calling toString on given value)</li>
	 * <li>EntityType: value defined by {@link SelectiveInformation} annotation, or type signature with id and
	 * {@link GenericEntity#getPartition() partition} as default.</li>
	 * <li>EnumType: the name of given constant</li>
	 * <li>CollectionType: This is something weird now - number of elements.</li>
	 * </ul>
	 */
	String getSelectiveInformation(Object instance);

	/**
	 * This method returns in the most specified type statement seen from assignable perspective.
	 */
	GenericModelType getActualType(Object value);

	/**
	 * Returns a "snapshot" (i.e. current state) of given value. Typical use-case is the value to be set for a
	 * manipulation (e.g. ChangeValueManipulation). This value may in most cases be the original object, but in case of
	 * collections (including maps), we have to create a copy (because if we used the original collection and changed it
	 * later, we would also change the manipulation).
	 */
	Object getValueSnapshot(Object value);

	/**
	 * @return the default value for the corresponding primitive type (which obviously only makes sense if this is one
	 *         of the relevant {@link SimpleType}s), or <tt>null</tt> in other cases.
	 */
	Object getDefaultValue();

	/**
	 * @return <code>true</code> iff given value is <code>null</code> or is an instance of this type.
	 * 
	 * @see #isInstance(Object)
	 */
	default boolean isValueAssignable(Object value) {
		return value == null || isInstance(value);
	}

	/**
	 * Equivalent of the 'instanceof' check in java, i.e.for <tt>null</tt> this returns <tt>false</tt>.
	 * 
	 * @see CollectionType#isInstance(Object)
	 */
	boolean isInstance(Object value);

	/** @return <code>true</code> iff the parameter <tt>type</tt> represents a sub-type of <tt>this</tt>. */
	boolean isAssignableFrom(GenericModelType type);

	/** @return true iff this is an instance of {@link BaseType}. */
	boolean isBase();

	/** @return true iff this is an instance of {@link SimpleType}. */
	boolean isSimple();

	/** @return true iff this is an instance one of the number types. */
	boolean isNumber();

	/** @return true iff this is an instance of {@link EntityType}. */
	boolean isEntity();

	/**
	 * @return true iff this is an instance of {@link EntityType} and instances would be of type {@link ValueDescriptor}
	 *         .
	 */
	boolean isVd();

	/** @return true iff this is an instance of {@link EnumType}. */
	boolean isEnum();

	/** @return true iff this is an instance of {@link CollectionType}. */
	boolean isCollection();

	<T extends GenericModelType> T cast();

	/**
	 * @return true if the you can reach any entities when traversing from an instance of this type
	 */
	boolean areEntitiesReachable();

	/**
	 * @return true if the you can reach any custom type instances when traversing from an instance of this type
	 */
	boolean areCustomInstancesReachable();

	/** @return true if this is an instance of {@link SimpleType} or {@link EnumType}. */
	boolean isScalar();

	boolean isEmpty(Object value);

	@Override
	int compareTo(GenericModelType o);

	GenericModelType[] getParameterization();

	// ################################################
	// ## . . . . . . traversing/cloning . . . . . . ##
	// ################################################

	void traverse(TraversingContext traversingContext, Object instance) throws GenericModelException;

	@JsIgnore
	TraversingContext traverse(Object instance, Matcher matcher, TraversingVisitor traversingVisitor) throws GenericModelException;

	@JsIgnore
	<T> T clone(Object instance, Matcher matcher, StrategyOnCriterionMatch strategy) throws GenericModelException;

	<T> T clone(CloningContext cloningContext, Object instance, StrategyOnCriterionMatch strategy) throws GenericModelException;

	/**
	 * Use the types directly - for list and set use {@link CollectionType#getCollectionElementType()}, for map use
	 * {@link MapType#getKeyType()} and {@link MapType#getValueType()}.
	 */

}
