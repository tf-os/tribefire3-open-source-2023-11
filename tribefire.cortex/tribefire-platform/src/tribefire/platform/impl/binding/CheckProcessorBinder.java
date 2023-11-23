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

import com.braintribe.model.extensiondeployment.check.CheckProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.DirectComponentBinder;
import com.braintribe.model.processing.deployment.api.MutableDeploymentContext;
import com.braintribe.model.processing.service.api.ServiceProcessor;

import tribefire.cortex.check.processing.TimeMeasuringCheckServiceProcessor;

public class CheckProcessorBinder extends AbstractSessionFactoryBasedBinder
		implements DirectComponentBinder<CheckProcessor, com.braintribe.model.processing.check.api.CheckProcessor> {

	public static final CheckProcessorBinder INSTANCE = new CheckProcessorBinder();
	
	
	@Override
	public Object bind(MutableDeploymentContext<CheckProcessor, com.braintribe.model.processing.check.api.CheckProcessor> context)
			throws DeploymentException {

		return new TimeMeasuringCheckServiceProcessor(context.getInstanceToBeBound());
	}


	@Override
	public EntityType<CheckProcessor> componentType() {
		return CheckProcessor.T;
	}

	@Override
	public Class<?>[] componentInterfaces() {
		return new Class<?>[] { ServiceProcessor.class };
	}

}
