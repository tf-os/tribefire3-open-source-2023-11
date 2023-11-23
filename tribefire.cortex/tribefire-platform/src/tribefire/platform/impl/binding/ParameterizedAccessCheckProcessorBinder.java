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

import com.braintribe.cartridge.common.processing.accessrequest.InternalizingAccessRequestProcessor;
import com.braintribe.model.accessapi.AccessRequest;
import com.braintribe.model.check.service.CheckResult;
import com.braintribe.model.check.service.ParameterizedCheckRequest;
import com.braintribe.model.extensiondeployment.check.ParameterizedCheckProcessor;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.accessrequest.api.AccessRequestProcessor;
import com.braintribe.model.processing.check.api.ParameterizedAccessCheckProcessor;
import com.braintribe.model.processing.deployment.api.ComponentBinder;
import com.braintribe.model.processing.deployment.api.DeploymentException;
import com.braintribe.model.processing.deployment.api.MutableDeploymentContext;
import com.braintribe.model.processing.service.api.ServiceProcessor;

public class ParameterizedAccessCheckProcessorBinder extends AbstractSessionFactoryBasedBinder
		implements ComponentBinder<ParameterizedCheckProcessor, com.braintribe.model.processing.check.api.ParameterizedAccessCheckProcessor<? extends AccessRequest>> {

	@Override
	public Object bind(MutableDeploymentContext<ParameterizedCheckProcessor, com.braintribe.model.processing.check.api.ParameterizedAccessCheckProcessor<? extends AccessRequest>> context)
			throws DeploymentException {
		return enrich(context.getInstanceToBeBound());
	}
	
	private <T extends ParameterizedCheckRequest & AccessRequest> InternalizingAccessRequestProcessor<T, CheckResult> enrich(ParameterizedAccessCheckProcessor<T> processor) {
		AccessRequestProcessor<T, CheckResult> accessRequestProcessor = processor::check;
		InternalizingAccessRequestProcessor<T, CheckResult> internalizer = new InternalizingAccessRequestProcessor<>(
				accessRequestProcessor, requestSessionFactory, systemSessionFactory);

		return internalizer;
	}

	@Override
	public EntityType<com.braintribe.model.extensiondeployment.ServiceProcessor> componentType() {
		return com.braintribe.model.extensiondeployment.ServiceProcessor.T;
	}

	@Override
	public Class<?>[] componentInterfaces() {
		return new Class<?>[] { ServiceProcessor.class };
	}

}
