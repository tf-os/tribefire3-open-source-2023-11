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

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.value.EntityReference;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
@SuppressWarnings("unusable-by-js")
public interface EntityType<T extends GenericEntity> extends EntityTypeDeprecations<T>, CustomType, EnhancableCustomType {

	/**
	 * @return the java interface, which defines the getters and setters for the properties. Following holds:
	 *         <code>this.getJavaType().getName().equals(this.getTypeSignature()) </code>
	 */
	@Override
	Class<T> getJavaType();

	boolean isAbstract();

	/** returns the direct supertypes of this {@link EntityType} */
	List<EntityType<?>> getSuperTypes();

	Iterable<EntityType<?>> getTransitiveSuperTypes(boolean includeSelf, boolean distinct);

	boolean hasExplicitSelectiveInformation();

	/** @return id property of this entity type, or <tt>null</tt> if this type has no id property */
	Property getIdProperty();

	/**
	 * @return an unmodifiable list of all the entity's properties (including those inherited from super types), sorted by name.
	 */
	List<Property> getProperties();

	/**
	 * @return similar to {@link #findProperty(String)}, but throws a {@link GenericModelException} if no property was found. Note that this works for
	 *         all properties, also the inherited ones.
	 */
	Property getProperty(String name) throws GenericModelException;

	/**
	 * @return {@link Property} for given name, or <tt>null</tt> if no such property is defined for this entity type. Note that this works for all
	 *         properties, also the inherited ones.
	 */
	Property findProperty(String name);

	/** @return list with the portion of the properties that have types which allow to reach enums or entities */
	List<Property> getCustomTypeProperties();

	/** @return all properties where property.getDeclaringType == this. See {@link Property#getDeclaringType()}. */
	List<Property> getDeclaredProperties();

	/**
	 * @return an unmodifiable list of all the entity's properties (including those inherited from super types), sorted by name.
	 */
	List<TransientProperty> getTransientProperties();

	/**
	 * @return similar to {@link #findProperty(String)}, but throws a {@link GenericModelException} if no property was found. Note that this works for
	 *         all properties, also the inherited ones.
	 */
	TransientProperty getTransientProperty(String name) throws GenericModelException;

	/**
	 * @return {@link Property} for given name, or <tt>null</tt> if no such property is defined for this entity type. Note that this works for all
	 *         properties, also the inherited ones.
	 */
	TransientProperty findTransientProperty(String name);

	/**
	 * All properties where property.getDeclaringType == this. See {@link Property#getDeclaringType()}.
	 */
	List<TransientProperty> getDeclaredTransientProperties();

	Stream<Attribute> getAttributes();

	/**
	 * Returns the declared type this entity evaluates to, or <tt>null</tt> if this type is not evaluable (see the following example).
	 * <p>
	 * In case the entity is declared as java interface, e.g. EvaluableEntity, the "evaluatesTo" value corresponds to the optional method:
	 * {@code EvalContext<X> eval(Evaluator<EvaluableEntity> evaluator);}. In this case we'd say this entity evaluates to {@code X}, and we call an
	 * entity with such a method "evaluable".
	 * <p>
	 * Note that this does not reflect the information inherited from super-types, for that see {@link #getEffectiveEvaluatesTo()}.
	 */
	GenericModelType getEvaluatesTo();

	/**
	 * Returns the type this entity evaluates to, which is either the {@link #getEvaluatesTo() evaluates to declared on this type} or inherited from
	 * one of it's super-types. If the entity is not evaluable, this method returns <tt>null</tt>
	 * <p>
	 * In case multiple supertypes are evaluable, the most specific type is returned.
	 * 
	 * @see #getEvaluatesTo()
	 */
	GenericModelType getEffectiveEvaluatesTo();

	/** {@inheritDoc} */
	@Override
	boolean isAssignableFrom(GenericModelType type);

	/** Similar to GenericModelType#isAssignableFrom(GenericModelType), this is here for convenience. */
	@JsIgnore
	boolean isAssignableFrom(EntityType<?> entityType);

	/**
	 * Creates reference on given entity with given id. The reason id is passed separately is that sometimes we want to create an reference with a
	 * different id, most probably when processing ChangeValueManipulations on id properties and say creating a reference for inverse manipulation
	 * based on the previous value, that is no longer set inside the entity.
	 * 
	 * TODO really think about how the {@link GenericEntity#partition} property is affected.
	 */
	EntityReference createReference(T entity, Object idValue);

	/**
	 * Creates a reference with given globalId. If the provided globalId is null, a preliminary reference is created.
	 */
	EntityReference createGlobalReference(T entity, String globalId);

	<E extends T> E initialize(E entity);

	// #########################################
	// ## . . . . . . new methods . . . . . . ##
	// #########################################

	@Override
	T createPlain();

	@Override
	T create();

	@JsIgnore
	T create(String globalId);

	@JsIgnore
	T create(PropertyAccessInterceptor pai);

	T createPlainRaw();

	T createRaw();

	@JsIgnore
	T createRaw(PropertyAccessInterceptor pai);

	Class<? extends T> plainClass();

	// maybe for JVM only
	Class<? extends T> enhancedClass();

	/** Creates an instance via {@link #create()} and initializes it with a {@link Consumer initializer} to enable flow-style creation. */
	@JsIgnore
	default T create(Consumer<? super T> initializer) {
		T entity = create();
		initializer.accept(entity);
		return entity;
	}

	/** Creates an instance via {@link GmSession#create(EntityType)}. */
	@JsIgnore
	default T create(GmSession session) {
		return session.create(this);
	}

	/**
	 * Creates an instance via {@link GmSession#create(EntityType)} and initializes it with a {@link Consumer initializer} to enable flow-style
	 * creation.
	 */
	@JsIgnore
	default T create(GmSession session, Consumer<? super T> initializer) {
		T entity = session.create(this);
		initializer.accept(entity);
		return entity;
	}

	/**
	 * Creates an instance via {@link Supplier#get() factory.get()} and initializes it with a {@link Consumer initializer} to enable flow-style
	 * creation.
	 */
	@JsIgnore
	default T create(Supplier<? extends T> factory, Consumer<? super T> initializer) {
		T entity = factory.get();
		initializer.accept(entity);
		return entity;
	}

}
