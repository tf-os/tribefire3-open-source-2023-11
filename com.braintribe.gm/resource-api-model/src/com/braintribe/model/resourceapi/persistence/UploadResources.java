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
package com.braintribe.model.resourceapi.persistence;

import java.util.List;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.resourceapi.base.ResourceRequest;
import com.braintribe.model.service.api.ServiceRequest;

public interface UploadResources extends ResourceRequest {

	EntityType<UploadResources> T = EntityTypes.T(UploadResources.class);

	String getUseCase();
	void setUseCase(String useCase);

	String getSourceType();
	void setSourceType(String sourceType);

	List<Resource> getResources();
	void setResources(List<Resource> resource);

	@Description("Whether mime type detection for the uploaded resources should be run, even if the Resource entity already has the mimeType property set. "
			+ "True: Run always, even if it was already set. False: Run never. if not set, set to application/octet-stream. "
			+ "null (default): run only if mime type was not already set on the Resource entity.")
	Boolean getDetectMimeType();
	void setDetectMimeType(Boolean detectMimeType);

	@Override
	EvalContext<? extends UploadResourcesResponse> eval(Evaluator<ServiceRequest> evaluator);

}
