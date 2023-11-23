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
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;
import com.braintribe.model.processing.session.impl.BasicPersistenceGmSessionFactory;

public class GmLocalSessionFactoryBuilderImpl extends GmSessionFactoryBuilderImpl implements GmLocalSessionFactoryBuilder {

	protected AccessService configuredAccessService;	
	protected Supplier<SessionAuthorization> configuredSessionAuthorizationProvider;
	protected SessionAuthorization configuredSessionAuthorization;

	/** {@inheritDoc} */
	@Override
	public GmLocalSessionFactoryBuilder modelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		super.configuredModelAccessoryFactory = modelAccessoryFactory;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public GmLocalSessionFactoryBuilder accessService(AccessService accessService) {
		this.configuredAccessService = accessService;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public GmLocalSessionFactoryBuilder resourceAccessFactory(ResourceAccessFactory<PersistenceGmSession> resourceAccessFactory) {
		this.configuredResourceAccessFactory = resourceAccessFactory;
		return this;
	}
	
	/** {@inheritDoc} */
	@Override
	public GmLocalSessionFactoryBuilder sessionAuthorizationProvider(Supplier<SessionAuthorization> sessionAuthorizationProvider) {
		this.configuredSessionAuthorizationProvider = sessionAuthorizationProvider;
		return this;
	}

	/** {@inheritDoc} */
	@Override
	public GmLocalSessionFactoryBuilder sessionAuthorization(SessionAuthorization sessionAuthorization) {
		this.configuredSessionAuthorization = sessionAuthorization;
		return this;
	}


	/** {@inheritDoc} */
	@Override
	public PersistenceGmSessionFactory done() throws GmSessionFactoryBuilderException {
		if (configuredAccessService == null) {
			throw new GmSessionFactoryBuilderException("The accessService must be set.");
		}
		if (this.configuredResourceAccessFactory == null) {
			throw new GmSessionFactoryBuilderException("The resourceAccessFactory must be set.");
		}
		try {
			BasicPersistenceGmSessionFactory factory = new BasicPersistenceGmSessionFactory();
			if (super.configuredModelAccessoryFactory != null) {
				factory.setModelAccessoryFactory(super.configuredModelAccessoryFactory);
			} 
			factory.setAccessService(this.configuredAccessService);
			factory.setResourceAccessFactory(this.configuredResourceAccessFactory);
			if (this.configuredSessionAuthorizationProvider != null) {
				factory.setSessionAuthorizationProvider(this.configuredSessionAuthorizationProvider);
			}
			if (this.configuredSessionAuthorization != null) {
				factory.setSessionAuthorization(this.configuredSessionAuthorization);
			}
			return factory;
		} catch(Exception e) {
			throw new GmSessionFactoryBuilderException("Could not create a LocalGmSessionFactory", e);
		}
	}

}
