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
package com.braintribe.model.processing.session;

import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;

/**
 * Base interface for session factory builders. There are two sub-classes of this interface: 
 * {@link GmLocalSessionFactoryBuilder} (for creating a session within tribefire) and {@link GmRemoteSessionFactoryBuilder}
 * for creating sessions to a remote tribefire instance.
 */
public interface GmSessionFactoryBuilder {

	/**
	 * Specify a custom {@link ModelAccessoryFactory}. This method is optional and can usually be omitted.
	 * @param modelAccessoryFactory The {@link ModelAccessoryFactory} that should be used for the client session.
	 * @return The {@link GmSessionFactoryBuilder} that offers this method.
	 */
	GmSessionFactoryBuilder modelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory);

	/**
	 * Sets the {@link ResourceAccessFactory} to be used by the session factory builder. Within a Cartridge, <pre>ClientBaseContract.resourceAccessFactory()</pre> could be
	 * used to get an resource access factory.
	 * @param resourceAccessFactory The {@link ResourceAccessFactory} that should be used to access resources. 
	 * @return The {@link GmSessionFactoryBuilder} instance currently used.
	 */
	GmSessionFactoryBuilder resourceAccessFactory(ResourceAccessFactory<PersistenceGmSession> resourceAccessFactory);

	/**
	 * Finishes the {@link GmSessionFactoryBuilder} and creates the resulting {@link PersistenceGmSessionFactory} based on the configuration
	 * provided.
	 * @return A {@link PersistenceGmSessionFactory} based on the configuration.
	 * @throws GmSessionFactoryBuilderException Thrown when the {@link PersistenceGmSessionFactory} could not be created.
	 */
	PersistenceGmSessionFactory done() throws GmSessionFactoryBuilderException;
}
