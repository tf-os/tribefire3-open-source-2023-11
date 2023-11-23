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
package com.braintribe.model.aws.service;

import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

@Description("Generates a pre-signed URL for a given Access ID and Resource ID that can be used for direct download for a given time.")
public interface CreatePresignedUrlForResource extends AwsRequest {

	EntityType<CreatePresignedUrlForResource> T = EntityTypes.T(CreatePresignedUrlForResource.class);

	@Name("Access ID")
	@Description("The access ID of the Resource")
	@Mandatory
	String getAccessId();
	void setAccessId(String accessId);

	@Name("Resource ID")
	@Description("The ID of the Resource")
	@Mandatory
	String getResourceId();
	void setResourceId(String resourceId);

	@Name("Time to Live (ms)")
	@Description("Specifies how long the link should be valid (in milliseconds)")
	long getTimeToLiveInMs();
	void setTimeToLiveInMs(long timeToLiveInMs);

	@Override
	EvalContext<? extends PresignedUrl> eval(Evaluator<ServiceRequest> evaluator);

}
