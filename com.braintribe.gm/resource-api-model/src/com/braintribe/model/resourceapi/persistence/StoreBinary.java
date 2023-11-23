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

import com.braintribe.model.accessapi.AccessDataRequest;
import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.eval.EvalContext;
import com.braintribe.model.generic.eval.Evaluator;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;
import com.braintribe.model.service.api.ServiceRequest;

public interface StoreBinary extends BinaryPersistenceRequest, AccessDataRequest {

	EntityType<StoreBinary> T = EntityTypes.T(StoreBinary.class);

	Resource getCreateFrom();
	void setCreateFrom(Resource createFrom);

	/**
	 * Most implementations for this requests used to persist the Resource entity of the {@link StoreBinaryResponse
	 * response} into the access. This should however not be its responsibility but should happen (and from now on will
	 * happen) during {@link UploadResource}. This flag exists just for backwards compatibility reasons but must,
	 * because of this, be explicitly set to false. Please always set it to false and make sure the {@link Resource} is
	 * persisted afterwards (if needed).
	 */
	@Initializer("true")
	boolean getPersistResource();
	void setPersistResource(boolean persistResource);

	@Override
	EvalContext<? extends StoreBinaryResponse> eval(Evaluator<ServiceRequest> evaluator);

}
