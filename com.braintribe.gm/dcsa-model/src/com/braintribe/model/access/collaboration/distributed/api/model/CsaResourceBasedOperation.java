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
package com.braintribe.model.access.collaboration.distributed.api.model;

import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.resource.Resource;

/**
 * Base for {@link CsaOperation}s that are carrying a payload - either a GMML snippet {@link CsaAppendDataManipulation},
 * {@link CsaAppendModelManipulation}, or ones related to actual binary data, e.g. {@link CsaStoreResource}.
 * 
 * @author peter.gazdik
 */
@Abstract
public interface CsaResourceBasedOperation extends CsaOperation {

	EntityType<CsaResourceBasedOperation> T = EntityTypes.T(CsaResourceBasedOperation.class);

	String payload = "payload";

	Resource getPayload();
	void setPayload(Resource payload);

}
