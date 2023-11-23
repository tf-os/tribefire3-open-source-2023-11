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
package com.braintribe.model.accessory;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmMetaModel;

/**
 * Resolves a model by it's {@link GmMetaModel#getName() name}.
 * 
 * @author peter.gazdik
 */
public interface GetModelByName extends ModelRetrievingRequest {

	EntityType<GetModelByName> T = EntityTypes.T(GetModelByName.class);

	static GetModelByName create(String name, String perspective) {
		GetModelByName result = GetModelByName.T.create();
		result.setName(name);
		result.setPerspective(perspective);

		return result;
	}

	/** Name of the model we are getting. */
	String getName();
	void setName(String name);

}
