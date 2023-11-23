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
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Name;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;

@Description("Resource download request.")
@Name("Resource Download")
public interface ResourceDownloadRequest extends ResourceStreamingRequest {

	EntityType<ResourceDownloadRequest> T = EntityTypes.T(ResourceDownloadRequest.class);

	@Mandatory
	@Description("The ID of the resource that should be downloaded. This can be obtained by viewing the resource in Control Center "
			+ "or using a REST query to discover said resource.")
	String getResourceId();
	void setResourceId(String resourceId);

	@Description("Point from where to start read resource. Default first byte.")
	@Initializer("0l")
	long getRangeStart();
	void setRangeStart(long rangeStart);

	@Description("Point where to stop read resource. Default last byte.")
	@Initializer("0l")
	long getRangeEnd();
	void setRangeEnd(long rangeEnd);

	@Description("Indicates resource modified since.")
	String getIfModifiedSince();
	void setIfModifiedSince(String ifModifiedSince);

	@Description("Defaults to false, meaning that caching will be active. If provide with the value true Cache-Control headers will be present "
			+ "in the response header to avoid caching.")
	@Initializer("false")
	boolean getNoCache();
	void setNoCache(boolean noCache);

	@Description("Boolean value, if true: response header Content-Type will be set to <b>application/download</b> instead of the resource's mime type, "
			+ "response header Content-Disposition will be set to attachment")
	@Initializer("false")
	boolean getDownload();
	void setDownload(boolean download);

	@Description("Condition in case of resource fingerprint mismatch.")
	String getIfNoneMatch();
	void setIfNoneMatch(String ifNoneMatch);

	@Override
	EvalContext<Resource> eval(Evaluator<ServiceRequest> evaluator);

}
