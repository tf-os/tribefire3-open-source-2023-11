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

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmMetaModel;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.ServiceRequest;
import com.braintribe.model.swagger.v2_0.SwaggerApi;

@Abstract
@Description("Returns the SwaggerApi for the requested domain.")
public interface SwaggerRequest extends AuthorizedRequest {

	EntityType<SwaggerRequest> T = EntityTypes.T(SwaggerRequest.class);

	@Override
	default boolean system() {
		return true;
	}

	String getResource();
	void setResource(String resource);

	String getBasePath();
	void setBasePath(String basePath);

	@Mandatory
	@Initializer("'2.0'")
	String getVersion();
	void setVersion(String version);

	GmMetaModel getModel();
	void setModel(GmMetaModel model);

	@Initializer("true")
	boolean getUseFullyQualifiedDefinitionName();
	void setUseFullyQualifiedDefinitionName(boolean useFullyQualifiedDefinitionName);

	@Initializer("false")
	boolean getUseJSONForExport();
	void setUseJSONForExport(boolean value);

	@Override
	EvalContext<? extends SwaggerApi> eval(Evaluator<ServiceRequest> evaluator);

}
