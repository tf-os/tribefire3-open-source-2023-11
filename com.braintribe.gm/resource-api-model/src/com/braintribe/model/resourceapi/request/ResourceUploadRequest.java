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

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;

@Description("Resource upload request.")
@Name("Resource Upload")
public interface ResourceUploadRequest extends ResourceStreamingRequest {

	EntityType<ResourceUploadRequest> T = EntityTypes.T(ResourceUploadRequest.class);

	@Description("Determines the name of the new Resource created in tribefire. This parameter is only required when the upload is performed with "
			+ "the <b>application/x-www-form-urlencoded</b> body content type.")
	String getFileName();
	void setFileName(String fileName);

	@Description("When uploading a new resource it will be created in tribefire associated with a Resource entity instance. "
			+ "This new Resource instance is returned in the body of the response and is by default of the type <b>application/json</b>. "
			+ "You can determine the type that response should take by using this parameter.")
	@Initializer("'application/json'")
	String getResponseMimeType();
	void setResponseMimeType(String responseMimeType);

	@Description("Resource upload resource use case.")
	String getUseCase();
	void setUseCase(String useCase);

	@Description("Mime Type a resource format identifier.")
	String getMimeType();
	void setMimeType(String mimeType);

	@Description("The MD5 digital fingerprint of a file.")
	String getMd5();
	void setMd5(String md5);

	@Description("Fully qualified name of EntityType that extends com.braintribe.model.resource.source.ResourceSource.")
	String getSourceType();
	void setSourceType(String sourceType);

	@Override
	EvalContext<Resource> eval(Evaluator<ServiceRequest> evaluator);

}
