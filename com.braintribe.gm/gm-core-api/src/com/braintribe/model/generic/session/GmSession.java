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
package com.braintribe.model.generic.session;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.enhance.EnhancedEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.generic.tracking.ManipulationListener;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * A <tt>GmSession</tt> represents a "bundle" that "binds" together entities. Apart from creating/deleting entities the main purpose is to provide
 * common manipulation tracking and AOP for a group of entities. When it comes to AOP, each getter/setter of the enhanced instance uses the
 * {@link PropertyAccessInterceptor} of the entity's session, retrieved vie {@link #getInterceptor()}.
 * 
 * @see ManipulationListener
 * @see PropertyAccessInterceptor
 */
@JsType(namespace = GmCoreApiInteropNamespaces.session)
@SuppressWarnings("unusable-by-js")
public interface GmSession extends ManipulationListener {

	<T extends GenericEntity> T create(EntityType<T> entityType);

	<T extends GenericEntity> T createRaw(EntityType<T> entityType);

	// TODO extract to new SuperAbstractGmSession (common super-type for all)
	@JsIgnore
	default <T extends GenericEntity> T create(EntityType<T> entityType, String globalId) {
		T result = create(entityType);
		result.setGlobalId(globalId);
		return result;
	}

	@JsIgnore
	default <T extends GenericEntity> T createRaw(EntityType<T> entityType, String globalId) {
		T result = createRaw(entityType);
		result.setGlobalId(globalId);
		return result;
	}

	/**
	 * Attaches given entity to this session. Given entity must be an {@link EnhancedEntity}, and the "attachment" is maybe via invoking
	 * {@link EnhancedEntity#attachSession(GmSession)}.
	 * <p>
	 * Note that the attachment process is not transitive, i.e. if given <tt>entity</tt> references other entities which are not attached to the
	 * session yet, this method DOES NOT do this automatically. This is not desired due to performance reasons. (We might add another method/flag to
	 * make this possible later.)
	 * 
	 * @throws GmSessionRuntimeException
	 *             if given entity is not an instance of {@link EnhancedEntity} (or some other problem, although this is the only case at the moment)
	 */
	void attach(GenericEntity entity) throws GmSessionRuntimeException;

	/**
	 * Removes given entity from session, i.e. What this actually does is it creates an instance of DeleteManipulation and notices itself with it (As
	 * {@linkplain GmSession} is also a {@link ManipulationListener}).
	 * 
	 * @throws GmSessionRuntimeException
	 *             if given entity is attached to a different session
	 */
	void deleteEntity(GenericEntity entity) throws GmSessionRuntimeException;

	@SuppressWarnings("unusable-by-js")
	PropertyAccessInterceptor getInterceptor();

}
