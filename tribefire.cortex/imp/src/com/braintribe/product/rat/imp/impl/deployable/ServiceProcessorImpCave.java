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
package com.braintribe.product.rat.imp.impl.deployable;

import com.braintribe.model.extensiondeployment.ServiceProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.product.rat.imp.AbstractImpCave;

/**
 * An {@link AbstractImpCave} specialized in {@link ServiceProcessor}
 */
public class ServiceProcessorImpCave extends AbstractImpCave<ServiceProcessor, ServiceProcessorImp> {

	public ServiceProcessorImpCave(PersistenceGmSession session) {
		super(session, "externalId", ServiceProcessor.T);
	}

	@Override
	protected ServiceProcessorImp buildImp(ServiceProcessor instance) {
		return new ServiceProcessorImp(session(), instance);
	}

	public <T extends ServiceProcessor> ServiceProcessorImp create(EntityType<T> entityType, String name, String externalId) {
		T serviceProcessor = session().create(entityType);
		serviceProcessor.setName(name);
		serviceProcessor.setExternalId(externalId);
		return new ServiceProcessorImp(session(), serviceProcessor);
	}

}
