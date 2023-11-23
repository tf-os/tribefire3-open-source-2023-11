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
package tribefire.platform.impl.binding;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.DirectComponentBinder;
import com.braintribe.model.processing.deployment.api.MutableDeploymentContext;
import com.braintribe.model.processing.service.api.ServiceProcessor;

public class ServiceProcessorBinder implements DirectComponentBinder<com.braintribe.model.extensiondeployment.ServiceProcessor, ServiceProcessor<?, ?>> {

	public static final ServiceProcessorBinder INSTANCE = new ServiceProcessorBinder();

	private ServiceProcessorBinder() {
	}

	@Override
	public EntityType<com.braintribe.model.extensiondeployment.ServiceProcessor> componentType() {
		return com.braintribe.model.extensiondeployment.ServiceProcessor.T;
	}

	@Override
	public ServiceProcessor<?, ?> bind(MutableDeploymentContext<com.braintribe.model.extensiondeployment.ServiceProcessor, ServiceProcessor<?, ?>> context) throws DeploymentException {
		return context.getInstanceToBeBound();
	}

	@Override
	public Class<?>[] componentInterfaces() {
		return new Class<?>[] { ServiceProcessor.class };
	}

}
