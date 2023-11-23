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
package tribefire.platform.impl.preprocess;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.model.processing.ValidationExpertRegistry;
import com.braintribe.model.processing.Validator;
import com.braintribe.model.processing.impl.ValidatorImpl;
import com.braintribe.model.processing.meta.cmd.CmdResolver;
import com.braintribe.model.processing.service.api.ServicePreProcessor;
import com.braintribe.model.processing.service.api.ServiceRequestContext;
import com.braintribe.model.processing.session.api.managed.ModelAccessoryFactory;
import com.braintribe.model.service.api.ServiceRequest;

public class RequestValidatorPreProcessor implements ServicePreProcessor<ServiceRequest> {
	private ModelAccessoryFactory modelAccessoryFactory;
	private ValidationExpertRegistry validationExpertRegistry;
	
	@Configurable
	@Required
	public void setValidationExpertRegistry(ValidationExpertRegistry validationExpertRegistry) {
		this.validationExpertRegistry = validationExpertRegistry;
	}
	
	@Configurable
	@Required
	public void setModelAccessoryFactory(ModelAccessoryFactory modelAccessoryFactory) {
		this.modelAccessoryFactory = modelAccessoryFactory;
	}
	
	@Override
	public ServiceRequest process(ServiceRequestContext requestContext, ServiceRequest request) {
		
		CmdResolver serviceDomainCmdResolver = modelAccessoryFactory.getForServiceDomain(requestContext.getDomainId()).getCmdResolver();
		Validator validator = new ValidatorImpl(serviceDomainCmdResolver, validationExpertRegistry);
		validator.validate(request);
		
		return request;
	}
	
}
