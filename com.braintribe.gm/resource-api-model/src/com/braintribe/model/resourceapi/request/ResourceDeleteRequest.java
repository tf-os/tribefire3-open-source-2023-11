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
package com.braintribe.model.resourceapi.request;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;

@Description("Resource delete request.")
@Name("Resource Delete")
public interface ResourceDeleteRequest extends ResourceStreamingRequest {

	EntityType<ResourceDeleteRequest> T = EntityTypes.T(ResourceDeleteRequest.class);

	@Mandatory
	@Description("The ID of the resource that should be downloaded. This can be obtained by viewing the resource in Control Center "
			+ "or using a REST query to discover said resource.")
	String getResourceId();
	void setResourceId(String resourceId);

	@Description("Resource delete use case.")
	String getUseCase();
	void setUseCase(String useCase);

	@Override
	EvalContext<Resource> eval(Evaluator<ServiceRequest> evaluator);

}
