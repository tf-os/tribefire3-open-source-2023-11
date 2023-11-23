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
package com.braintribe.model.elasticsearch.service;

import java.util.Set;

import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;

public interface ReIndexEntity extends ElasticRequest {
	final EntityType<ReIndexEntity> T = EntityTypes.T(ReIndexEntity.class);

	String getAccessId();
	void setAccessId(String accessId);

	String getTypeSignature();
	void setTypeSignature(String typeSignature);

	Object getEntityId();
	void setEntityId(Object entityId);

	Set<Resource> getResources();
	void setResources(Set<Resource> resources);

	@Override
	EvalContext<? extends ReIndexEntitiesResult> eval(Evaluator<ServiceRequest> evaluator);

}
