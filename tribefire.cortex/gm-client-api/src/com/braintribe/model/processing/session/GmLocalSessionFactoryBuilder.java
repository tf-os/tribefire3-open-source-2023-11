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

import java.util.function.Supplier;

import com.braintribe.model.access.AccessService;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;

/**
 * Interface for a session factory builder that is able to create a session within tribefire. Both the
 * {@link #resourceAccessFactory(ResourceAccessFactory)} and the {@link #accessService(AccessService)} method must be used.
 */
public interface GmLocalSessionFactoryBuilder extends GmSessionFactoryBuilder {

	/**
	 * Provides the {@link AccessService} that could be used to access Accesses.
	 * 
	 * @param accessService
	 *            The {@link AccessService} that can be used to access Accesses.
	 * @return The {@link GmLocalSessionFactoryBuilder} instance currently used.
	 */
	GmLocalSessionFactoryBuilder accessService(AccessService accessService);

	/** {@inheritDoc} */
	@Override
	GmLocalSessionFactoryBuilder resourceAccessFactory(ResourceAccessFactory<PersistenceGmSession> resourceAccessFactory);

	/**
	 * Sets an optional {@link SessionAuthorization} provider.
	 * 
	 * @param sessionAuthorizationProvider
	 *            The {@link SessionAuthorization} that should be used.
	 * @return The {@link GmLocalSessionFactoryBuilder} instance currently used.
	 */
	GmLocalSessionFactoryBuilder sessionAuthorizationProvider(Supplier<SessionAuthorization> sessionAuthorizationProvider);

	/**
	 * Specifies a fixed {@link SessionAuthorization} object that will be used to authorize sessions.
	 * 
	 * @param sessionAuthorization
	 *            the {@link SessionAuthorization} object that should be used to authorize sessions.
	 * @return The {@link GmLocalSessionFactoryBuilder} instance currently used.
	 */
	GmLocalSessionFactoryBuilder sessionAuthorization(SessionAuthorization sessionAuthorization);
}
