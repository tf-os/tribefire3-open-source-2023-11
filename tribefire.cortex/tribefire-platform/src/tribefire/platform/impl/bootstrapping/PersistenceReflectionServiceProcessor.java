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
package tribefire.platform.impl.bootstrapping;

import com.braintribe.cfg.Configurable;
import com.braintribe.cfg.Required;
import com.braintribe.gm.model.persistence.reflection.api.GetAccessIds;
import com.braintribe.gm.model.persistence.reflection.api.GetMetaModel;
import com.braintribe.gm.model.persistence.reflection.api.GetMetaModelForTypes;
import com.braintribe.gm.model.persistence.reflection.api.GetModelAndWorkbenchEnvironment;
import com.braintribe.gm.model.persistence.reflection.api.GetModelEnvironment;
import com.braintribe.gm.model.persistence.reflection.api.GetModelEnvironmentForDomain;
import com.braintribe.gm.model.persistence.reflection.api.GetModelEnvironmentServices;
import com.braintribe.gm.model.persistence.reflection.api.GetModelEnvironmentServicesForDomain;
import com.braintribe.gm.model.persistence.reflection.api.PersistenceReflectionRequest;
import com.braintribe.model.access.AccessService;
import com.braintribe.model.processing.service.impl.AbstractDispatchingServiceProcessor;
import com.braintribe.model.processing.service.impl.DispatchConfiguration;

public class PersistenceReflectionServiceProcessor extends AbstractDispatchingServiceProcessor<PersistenceReflectionRequest, Object>{
	private AccessService accessService;
	
	@Configurable @Required
	public void setAccessService(AccessService accessService) {
		this.accessService = accessService;
	}
	
	@Override
	protected void configureDispatching(DispatchConfiguration<PersistenceReflectionRequest, Object> dispatching) {
		dispatching.register(GetMetaModel.T, // 
				(c,r) -> accessService.getMetaModel(r.getAccessId()));
		dispatching.register(GetModelAndWorkbenchEnvironment.T, // 
				(c,r) -> accessService.getModelAndWorkbenchEnvironment(r.getAccessId(), r.getFoldersByPerspective()));
		dispatching.register(GetModelEnvironment.T, // 
				(c,r) -> accessService.getModelEnvironment(r.getAccessId()));
		dispatching.register(GetModelEnvironmentForDomain.T, // 
				(c,r) -> accessService.getModelEnvironmentForDomain(r.getAccessId(), r.getAccessDomain()));
		dispatching.register(GetModelEnvironmentServices.T, // 
				(c,r) -> accessService.getModelEnvironmentServices(r.getAccessId()));
		dispatching.register(GetModelEnvironmentServicesForDomain.T, // 
				(c,r) -> accessService.getModelEnvironmentServicesForDomain(r.getAccessId(), r.getAccessDomain()));
		dispatching.register(GetAccessIds.T, // 
				(c,r) -> accessService.getAccessIds());
		dispatching.register(GetMetaModelForTypes.T, // 
				(c,r) -> accessService.getMetaModelForTypes(r.getTypeSignatures()));
	}
}
