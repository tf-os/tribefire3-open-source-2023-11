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
package com.braintribe.model.swaggerapi;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.swagger.v2_0.SwaggerApi;

public interface SwaggerServicesRequest extends SwaggerRequest {

	EntityType<SwaggerServicesRequest> T = EntityTypes.T(SwaggerServicesRequest.class);

	@Description("The id of the service domain the SwaggerApi should be created for.")
	String getServiceDomain();
	void setServiceDomain(String serviceDomain);

	@Description("Display POST requests as multipart requests if not stated otherwise via mapping")
	@Initializer("true")
	boolean getDefaultToMultipart();
	void setDefaultToMultipart(boolean serviceDomain);

	@Override
	EvalContext<? extends SwaggerApi> eval(Evaluator<ServiceRequest> evaluator);

}
