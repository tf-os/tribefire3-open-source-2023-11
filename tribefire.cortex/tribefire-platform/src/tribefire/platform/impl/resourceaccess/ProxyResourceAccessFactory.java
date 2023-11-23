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
package tribefire.platform.impl.resourceaccess;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.accessdeployment.IncrementalAccess;
import com.braintribe.model.processing.deployment.api.DeployedComponentResolver;
import com.braintribe.model.processing.session.api.persistence.PersistenceGmSession;
import com.braintribe.model.processing.session.api.resource.ResourceAccess;
import com.braintribe.model.processing.session.api.resource.ResourceAccessFactory;

public class ProxyResourceAccessFactory implements ResourceAccessFactory<PersistenceGmSession> {

	private DeployedComponentResolver deployedComponentResolver;

	@Configurable @Required
	public void setDeployedComponentResolver(DeployedComponentResolver deployedComponentResolver) {
		this.deployedComponentResolver = deployedComponentResolver;
	}

	@Override
	public ResourceAccess newInstance(PersistenceGmSession session) {
		String externalId = session.getAccessId();
		// works because MasterIncrementalAccess implements ResourceAccessFactory 
		ResourceAccessFactory<PersistenceGmSession> resourceAccessFactory = deployedComponentResolver.resolve(externalId, IncrementalAccess.T);
		return resourceAccessFactory.newInstance(session);
	}
}
