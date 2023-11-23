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
package com.braintribe.model.processing.session.impl;

import java.util.function.Supplier;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.access.AccessService;
import com.braintribe.model.access.impl.AccessServiceDelegatingAccess;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.session.exception.GmSessionException;
import com.braintribe.model.generic.session.exception.GmSessionRuntimeException;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;
import com.braintribe.model.processing.session.api.persistence.auth.SessionAuthorization;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;
import com.braintribe.model.processing.session.impl.persistence.BasicPersistenceGmSession;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.provider.Holder;

/**
 * This should in almost all cases be the only necessary implementation of {@link PersistenceGmSessionFactory}, because
 * all its functionality can be evaluated via DDSA in a normalized way. E.g. you can get local or remote sessions
 * depending on how you configure it - i.e. the used evaluator.
 * 
 * @author Neidhart.Orlich
 * @author Michael.Lafite
 *
 */
public class BasicPersistenceGmSessionFactory implements PersistenceGmSessionFactory {

	private AccessService accessService;
	private ResourceAccessFactory<? super BasicPersistenceGmSession> resourceAccessFactory;
	private Supplier<SessionAuthorization> sessionAuthorizationProvider;
	private ModelAccessoryFactory modelAccessoryFactory;
	private Evaluator<ServiceRequest> requestEvaluator;

	@Configurable
	@Required
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}

	@Override
	public PersistenceGmSession newSession(final String accessId) throws GmSessionException {
		try {

			final AccessServiceDelegatingAccess access = new AccessServiceDelegatingAccess();
			access.setAccessId(accessId);
			access.setAccessService(this.accessService);

			final BasicPersistenceGmSession persistenceGmSession = new BasicPersistenceGmSession();
			persistenceGmSession.setIncrementalAccess(access);
			persistenceGmSession.setResourcesAccessFactory(resourceAccessFactory);
			persistenceGmSession.setAccessId(accessId);
			persistenceGmSession.setRequestEvaluator(requestEvaluator);

			if (modelAccessoryFactory != null)
				persistenceGmSession.setModelAccessory(modelAccessoryFactory.getForAccess(accessId));

			if (sessionAuthorizationProvider != null)
				persistenceGmSession.setSessionAuthorization(sessionAuthorizationProvider.get());

			return persistenceGmSession;

		} catch (final Exception e) {
			throw new GmSessionRuntimeException("Could not create gm session.", e);
		}
	}

	public AccessService getAccessService() {
		return this.accessService;
	}

	@Required
	@Configurable
	public void setResourceAccessFactory(ResourceAccessFactory<? super BasicPersistenceGmSession> resourceAccessFactory) {
		this.resourceAccessFactory = resourceAccessFactory;
	}

	@Required
	@Configurable
	public void setAccessService(final AccessService accessService) {
		this.accessService = accessService;
	}

	@Configurable
	public void setSessionAuthorizationProvider(Supplier<SessionAuthorization> sessionAuthorizationProvider) {
		this.sessionAuthorizationProvider = sessionAuthorizationProvider;
	}

	@Configurable
	public void setSessionAuthorization(SessionAuthorization sessionAuthorization) {
		setSessionAuthorizationProvider(new Holder<>(sessionAuthorization));
	}

	@Configurable
	public void setRequestEvaluator(Evaluator<ServiceRequest> requestEvaluator) {
		this.requestEvaluator = requestEvaluator;
	}

}
