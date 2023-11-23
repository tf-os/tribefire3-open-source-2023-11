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
package com.braintribe.model.processing.cortex.service.workbench;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.access.AccessService;
import com.braintribe.model.cortexapi.workbench.CreateServiceRequestTemplate;
import com.braintribe.model.notification.Notifications;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSessionFactory;

public class CreateServiceRequestTemplateProcessor extends AbstractDispatchingServiceProcessor<CreateServiceRequestTemplate, Notifications> {

	private PersistenceGmSessionFactory sessionFactory;
	private AccessService accessService;

	@Override
	protected void configureDispatching(DispatchConfiguration<CreateServiceRequestTemplate, Notifications> dispatching) {
		dispatching.register(CreateServiceRequestTemplate.T, (c, r) -> createServiceRequestTemplate(r));
	}

	@Required
	@Configurable
	public void setSessionFactory(PersistenceGmSessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Required
	@Configurable
	public void setAccessService(AccessService accessService) {
		this.accessService = accessService;
	}

	public Notifications createServiceRequestTemplate(CreateServiceRequestTemplate request) {

		try {
			return new TemplateCreator(sessionFactory, request, accessService).run();
		} catch (Exception e) {
			throw new RuntimeException("Error while creating service request template for request: " + request, e);
		}

	}

}
