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
package com.braintribe.model.processing.session.impl.managed;

import com.braintribe.model.processing.session.api.managed.ManagedGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;

/**
 * @see AbstractManagedGmSession
 * @see ManagedGmSession
 */
public class BasicManagedGmSession extends AbstractManagedGmSession {

	private ResourceAccessFactory<? super BasicManagedGmSession> resourcesAccessFactory;
	private ResourceAccess resourcesAccess;

	public void setResourcesAccessFactory(ResourceAccessFactory<? super BasicManagedGmSession> resourcesAccessFactory) {
		this.resourcesAccessFactory = resourcesAccessFactory;
	}
	
	public void setResourcesAccess(ResourceAccess resourcesAccess) {
		this.resourcesAccess = resourcesAccess;
	}
	
	
	protected ResourceAccess getResourcesAccess() {
		if (resourcesAccess == null && resourcesAccessFactory != null) {
			resourcesAccess = resourcesAccessFactory.newInstance(this);
		}

		return resourcesAccess;
	}
	
	@Override
	public ResourceAccess resources() {
		ResourceAccess builder = getResourcesAccess();
		if (builder != null)
			return builder;
		else
			throw new UnsupportedOperationException("no resource builder configured for the session");
	}
}
