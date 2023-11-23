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
package com.braintribe.model.accessdeployment.smart.meta;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * Similar to {@link InverseKeyPropertyAssignment}, but when associating entities more properties are being compared.
 * This is only valid for 1:1 and 1:n relationships (i.e. the configured properties are something like a composite key
 * for the owner of this meta data, and there can be on or more "other" entities with given property values). So the
 * valid type of a property on which this is configured is an entity or a set of entities.
 */
public interface CompositeInverseKeyPropertyAssignment extends PropertyAssignment {

	EntityType<CompositeInverseKeyPropertyAssignment> T = EntityTypes.T(CompositeInverseKeyPropertyAssignment.class);

	// @formatter:off
	Set<InverseKeyPropertyAssignment> getInverseKeyPropertyAssignments();
	void setInverseKeyPropertyAssignments(Set<InverseKeyPropertyAssignment> inverseKeyPropertyAssignments);
	// @formatter:on

}
