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
package com.braintribe.model.generic.base;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.reflection.CloningContext;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.reflection.GenericModelType;
import com.braintribe.model.generic.reflection.Property;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.reflection.PropertyValueReceiver;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.value.EntityReference;
import com.braintribe.model.generic.value.ValueDescriptor;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * @see GenericBase
 */
@JsType(namespace = GmCoreApiInteropNamespaces.reflection)
@SuppressWarnings("unusable-by-js")
public interface EntityBase extends GenericBase {

	/** @deprecated Seems pretty pointless, use {@link GenericEntity#T} */
	@Deprecated
	final EntityType<GenericEntity> T = EntityTypes.T(GenericEntity.class);

	@Override @JsMethod(name="IsEntity")
	default boolean isEntity() {
		return true;
	}

	// ###################################################
	// ## . . . generic property-access methods . . . . ##
	// ###################################################

	@JsIgnore
	void write(Property p, Object value);
	@JsIgnore
	Object read(Property p);

	@JsIgnore
	void writeVd(Property p, ValueDescriptor value);
	@JsIgnore
	ValueDescriptor readVd(Property p);

	@JsIgnore
	void read(Property p, PropertyValueReceiver pvr);

	// ###################################################
	// ## . . . . . . other helpful methods . . . . . . ##
	// ###################################################

	/**
	 * Returns a technical description of this entity which is not meant to be displayed to the user. The default toString implementation delegates to {@link #asString()}
	 * <p>
	 * Use {@link ToStringInformation} annotation to specify a template to use for this method's implementation. 
	 * <p>
	 * Use {@link #toSelectiveInformation()} for a string description of this entity which is meant for the user.
	 */
	@Override
	@JsMethod(name="ToString")
	String toString();

	/** 
	 * Returns a string descriptor of this entity which is meant to be displayed to the user.
	 * <p>
	 * The default implementation .   
	 * <p> 
	 * Use {@link SelectiveInformation} annotation to specify a template to use for this method's implementation. 
	 * */
	@JsMethod(name="ToSelectiveInformation")
	String toSelectiveInformation();

	/**
	 * Returns a short (max one line) string representation of this entity.<br/> 
	 * The method is the delegate for {@link #toString()} if there is no {@link ToStringInformation}.
	 * <p>
	 * It is also meant for inline implementation in the declaration of an entity type. 
	 * 
	 * The default implementation uses {@link Entities#asString(GenericEntity)}
	 */
	@JsIgnore
	default String asString() {
		return Entities.asString((GenericEntity)this);
	}
	
	/**
	 * Returns a string representation of this entity. The default one is the {@link #toString()}, but for some
	 * hierarchies (queries, manipulations) a tailor-made stringifier might be in place. For details see the platform
	 * implementation.
	 */
	@JsMethod(name="Stringify")
	default String stringify() {
		return GMF.platform().stringify((GenericEntity) this);
	}

	/* TODO change return type to EntityType<?> once we change ITW so that entityType() method is created dynamically
	 * (and not this one) */
	@Override @JsMethod(name="Type")
	GenericModelType type();

	/**
	 * Returns the {@link EntityType} corresponding to this instance. This returns the same thing as {@link #type()}
	 * method, but with more specific return-type (as overriding that method with this return type is unfortunately not
	 * possible).
	 */
	@JsMethod(name="EntityType")
	<T extends GenericEntity> EntityType<T> entityType();

	@JsMethod(name="Reference")
	<T extends EntityReference> T reference();

	@JsMethod(name="GlobalReference")
	<T extends EntityReference> T globalReference();

	/** @return <tt>true</tt> iff this is an {@link EnhancedEntity} */
	@JsMethod(name="IsEnhanced")
	boolean isEnhanced();

	/** @return <tt>true</tt> iff the instance is of a {@linkplain ValueDescriptor} type */
	@JsMethod(name="IsVd")
	boolean isVd();
	
	/**
	 * Returns if the instance has any transient properties with a value that is non null;
	 */
	@JsMethod(name="HasTransientData")
	default boolean hasTransientData() { return false; }

	/**
	 * Returns an id which is unique for given instance for the entire runtime. This id has no correlation to the actual
	 * entity and only serves the purpose of unambiguously identifying entities and a stable ordering on them.
	 * <p>
	 * The use-case which led us to introduce this was the implementation of indices in Smood, since we wanted a
	 * "navigable"-like interface for our maps, but only had the "sorted" implementation, but with a little trick (using
	 * this id) we have found a workaround.
	 * <p>
	 * This is also be used for PreliminaryEntityReferences.
	 */
	@JsMethod(name="RuntimeId")
	long runtimeId();

	/** @return {@link GmSession} the entity is attached to or <tt>null</tt> if not attached to any session. */
	@JsMethod(name="Session")
	GmSession session();

	/**
	 * Attaches the instance to a {@link GmSession}. Only enhanced entities can actually be attached to a session. If a
	 * session is attached the {@link PropertyAccessInterceptor} are taken from the session.
	 */
	@JsMethod(name="Attach")
	void attach(GmSession session);

	/**
	 * Detaches the entity from the session if it is attached to one, otherwise it's a NO OP.
	 * 
	 * @return {@link GmSession} the entity was attached to or <tt>null</tt> if not attached to any session.
	 */
	@JsMethod(name="Detach")
	GmSession detach();

	/** @return clone of this entity based on given {@link CloningContext} (typically a StandardCloningContext) */
	@JsMethod(name="Clone")
	<T> T clone(CloningContext cloningContext);

	/** Traverse the entity according to given {@link TraversingContext} (typically a StandardTraversingContext). */
	@JsMethod(name="Traverse")
	void traverse(TraversingContext traversingContext);
}
