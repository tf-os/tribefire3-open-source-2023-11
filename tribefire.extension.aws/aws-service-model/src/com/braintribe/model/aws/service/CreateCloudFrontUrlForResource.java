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

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.annotation.meta.Description;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.service.api.ServiceRequest;

@Description("Generates a pre-signed URL for a given Access ID and Resource ID that can be used for direct download for a given time.")
public interface CreateCloudFrontUrlForResource extends AwsRequest {

	EntityType<CreateCloudFrontUrlForResource> T = EntityTypes.T(CreateCloudFrontUrlForResource.class);

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

	@Name("Time to Live (min)")
	@Description("Specifies how long the link should be valid (in minutes)")
	@Initializer("120") // 2h
	int getTimeToLiveInMin();
	void setTimeToLiveInMin(int timeToLiveInMin);

	@Name("Pre-sign URL")
	@Description("Determines whether the URL should be pre-signed or not. If left null, the URL will be pre-signed if there is a valid configuration available.")
	Boolean getPreSignUrl();
	void setPreSignUrl(Boolean preSignUrl);

	@Override
	EvalContext<? extends CloudFrontUrl> eval(Evaluator<ServiceRequest> evaluator);

}
