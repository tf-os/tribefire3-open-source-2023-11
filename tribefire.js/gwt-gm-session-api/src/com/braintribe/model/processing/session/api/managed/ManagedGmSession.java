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
package com.braintribe.model.processing.session.api.managed;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.GmCoreApiInteropNamespaces;
import com.braintribe.model.generic.manipulation.DeleteMode;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.processing.session.api.notifying.NotifyingGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.resource.api.HasResourceReadAccess;

import jsinterop.annotations.JsMethod;
import jsinterop.annotations.JsType;

/**
 * An extension of {@link NotifyingGmSession} which provides some advanced functionality regarding the entities
 * belonging to this session. The main difference is, that this session is aware of the entities attached to it.
 * <p>
 * It is possible to query these entities and also to apply manipulations on them. An important aspect of a managed
 * session (and sub-types) is the so called identity management, which is a guarantee that queries for the same entity
 * (same means having same id) always return the same instance.
 * <p>
 * Another important change compared to {@link NotifyingGmSession} is the semantics of the
 * {@link #deleteEntity(GenericEntity)}.
 * 
 * <h3>Querying attached entities</h3>
 * 
 * This session keeps track of the entities attached to it and is able to evaluate queries on these entities and also
 * resolve entity references. If an entity for given reference is not attached to the session, <tt>null</tt> is returned
 * (unlike in case of {@link PersistenceGmSession}).
 * 
 * @see NotifyingGmSession
 * @see PersistenceGmSession
 * @see SessionQueryBuilder
 * @see ManipulationApplicationContextBuilder
 */
@JsType(namespace=GmCoreApiInteropNamespaces.session)
@SuppressWarnings("unusable-by-js")
public interface ManagedGmSession extends NotifyingGmSession, HasResourceReadAccess, EntityManager {

	<T extends GenericEntity> T acquire(EntityType<T> entityType, String globalId); 
	
	/**
	 * Deletes given entity from the session and clears all the references from other entities pointing to given entity
	 * (other entities bound to this same session of course).
	 */
	@Override
	void deleteEntity(GenericEntity entity);

	@Override
	@JsMethod(name = "deleteEntityWithMode")
	void deleteEntity(GenericEntity entity, DeleteMode deleteMode);

	/**
	 * Creates a {@link SessionQueryBuilder} that can be used to expressively build and execute all kinds of queries.
	 */
	SessionQueryBuilder query();

	/**
	 * Returns the {@link ModelAccessory} that can be used to access meta information for the model.
	 */
	ModelAccessory getModelAccessory();

	/**
	 * Creates a {@link ManipulationApplicationContextBuilder} that can be used to execute {@link Manipulation}s.
	 */
	ManipulationApplicationContextBuilder manipulate();

	/**
	 * Creates a {@link MergeBuilder} that allows to merge entities into the current session.
	 */
	MergeBuilder merge() throws GmSessionException;

	/**
	 * Provides the {@link ResourceAccess} that allows dealing with streams.
	 */
	@Override
	ResourceAccess resources();

}
