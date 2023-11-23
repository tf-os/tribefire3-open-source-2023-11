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
package com.braintribe.model.access.collaboration.distributed.tools;

import com.braintribe.model.access.collaboration.distributed.api.model.CsaDeleteResource;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaManagePersistence;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaResourceBasedOperation;
import com.braintribe.model.access.collaboration.distributed.api.model.CsaStoreResource;
import com.braintribe.model.cortexapi.access.collaboration.CollaborativePersistenceRequest;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.resource.Resource;

/**
 * @author peter.gazdik
 */
public interface CsaOperationBuilder {

	static CsaManagePersistence managePersistence(CollaborativePersistenceRequest request) {
		CsaManagePersistence result = CsaManagePersistence.T.create();
		result.setPersistenceRequest(request);

		return result;
	}

	// The ignoredResource is there to allow for a functional interface compatible with this and storeResource
	static CsaDeleteResource deleteResource(@SuppressWarnings("unused") Resource ignoredResource, String resourceRelativePath) {
		CsaDeleteResource result = CsaDeleteResource.T.create();
		result.setResourceRelativePath(resourceRelativePath);

		return result;
	}

	static CsaStoreResource storeResource(Resource payload, String resourceRelativePath) {
		CsaStoreResource result = resourceBasedOp(CsaStoreResource.T, payload);
		result.setResourceRelativePath(resourceRelativePath);

		return result;
	}

	static <O extends CsaResourceBasedOperation> O resourceBasedOp(EntityType<O> operationType, Resource payload) {
		O result = operationType.create();
		result.setPayload(payload);

		return result;
	}

}
