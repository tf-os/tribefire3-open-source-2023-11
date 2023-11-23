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

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.ServiceRequest;

import java.util.Map;

@Abstract
public interface ResourceStreamingRequest extends AuthorizedRequest {

	EntityType<ResourceStreamingRequest> T = EntityTypes.T(ResourceStreamingRequest.class);

	@Mandatory
	@Description("The external ID of the access where the resource that is being downloaded can be found.")
	@Initializer("'cortex'")
	String getAccessId();
	void setAccessId(String accessId);

	@Description("Context.")
	Map<String, String> getContext();
	void setContext(Map<String, String> context);

	@Override
	EvalContext<Resource> eval(Evaluator<ServiceRequest> evaluator);

}
