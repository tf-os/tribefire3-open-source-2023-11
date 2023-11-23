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
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.AuthorizedRequest;
import com.braintribe.model.service.api.DispatchableRequest;
import com.braintribe.model.service.api.DomainRequest;
import com.braintribe.model.service.api.ServiceRequest;

public interface GetPreview extends DomainRequest, DispatchableRequest, AuthorizedRequest {

	EntityType<GetPreview> T = EntityTypes.T(GetPreview.class);

	String getInstanceTypeSignature();
	void setInstanceTypeSignature(String instanceTypeSignature);

	String getInstanceId();
	void setInstanceId(String instanceId);

	String getTimestamp();
	void setTimestamp(String timestamp);

	Integer getPreferredHeight();
	void setPreferredHeight(Integer preferredHeight);

	Integer getPreferredWidth();
	void setPreferredWidth(Integer preferredWidth);

	String getPreferredMimeType();
	void setPreferredMimeType(String preferredMimeType);

	@Initializer("enum(com.braintribe.model.resourceapi.request.PreviewType,STANDARD)")
	PreviewType getPreviewType();
	void setPreviewType(PreviewType previewType);

	@Override
	EvalContext<? extends Resource> eval(Evaluator<ServiceRequest> evaluator);

}
