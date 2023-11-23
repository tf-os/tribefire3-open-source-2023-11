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
package com.braintribe.model.processing.session.api.notifying;

import java.util.Stack;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.ManifestationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.PropertyAccessInterceptor;
import com.braintribe.model.generic.session.GmSession;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;

/**
 * An extension of {@link GmSession} that serves as hub for noticing {@link Manipulation} events. Using the
 * {@link GenericManipulationListenerRegistry} it is possible to add {@link ManipulationListener}s for all the session
 * manipulations, or filtered just for given entity, or just a property of given entity.
 * <p>
 * This session also provides a more advanced control of {@link PropertyAccessInterceptor}s (compared to just
 * {@linkplain GmSession}). With {@link PropertyAccessInterceptorRegistry} accessible via {@link #interceptors()}
 * method, it is possible to add/remove such interceptors, and also control the relative order of interceptors.
 * 
 * <h3>Querying attached entities</h3>
 * 
 * This session does not provide the functionality to query entities attached to it.
 * 
 * @see GmSession
 * @see ManagedGmSession
 * @see PersistenceGmSession
 * @see GenericManipulationListenerRegistry
 * @see PropertyAccessInterceptorRegistry
 */
public interface NotifyingGmSession extends GmSession {

	/**
	 * {@inheritDoc}
	 * 
	 * This method also creates a {@link ManifestationManipulation} which is used to notify all the listeners.
	 */
	@Override
	void attach(GenericEntity entity) throws GmSessionRuntimeException;

	GenericManipulationListenerRegistry listeners();

	PropertyAccessInterceptorRegistry interceptors();

	Stack<CompoundNotification> getCompoundNotificationStack();
}
