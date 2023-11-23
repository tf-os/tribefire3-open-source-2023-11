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
package com.braintribe.model.accessapi;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface ManipulationResponse extends GenericEntity {

	final EntityType<ManipulationResponse> T = EntityTypes.T(ManipulationResponse.class);

	void setInducedManipulation(Manipulation inducedManipulation);
	Manipulation getInducedManipulation();

	/**
	 * RollbackManipulation is an inverse to the manipulation which was sent in the corresponding
	 * {@link ManipulationRequest}, which can be used for rollback - typically in case this request/response is just one
	 * part of some bigger transaction.
	 */
	Manipulation getRollbackManipulation();
	void setRollbackManipulation(Manipulation rollbackManipulation);

}
