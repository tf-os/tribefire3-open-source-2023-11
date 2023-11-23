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
package com.braintribe.model.processing.session.api.persistence;

import com.braintribe.model.generic.session.exception.GmSessionException;

/**
 * <p>Interface for a session factory. The purpose of this interface is to provide a method for creating
 * either a local (if you're operating within tribefire-services) or a remote {@link PersistenceGmSession} 
 * for a specific access Id.
 * </p>
 * <p>The recommended way to create a new session factory is by using <code>GmSessionFactories.remote(url)</code> from the
 * library <code>com.braintribe.tribefire.cortex:GmClientSupport</code>.
 * </p>
 * <p>
 * Example:<br><br>
 * <code>
 * PersistenceGmSessionFactory sessionFactory = GmSessionFactories.remote("https://localhost:8443/tribefire-services").authentication("cortex", "cortex").done();
 * </code>
 * </p>
 */
public interface PersistenceGmSessionFactory {

	public PersistenceGmSession newSession(String accessId) throws GmSessionException;
	
}
