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

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.meta.GmEntityType;

/**
 * Specifies the delegate entity (property: {@link #getEntityType() entityType}) to which our smart entity is mapped to.
 * 
 * This meta-data is only needed in cases where a subtype lacks any declared property that would normally tell the type
 * association
 */
public interface QualifiedEntityAssignment extends EntityAssignment {

	EntityType<QualifiedEntityAssignment> T = EntityTypes.T(QualifiedEntityAssignment.class);

	// @formatter:off
	GmEntityType getEntityType();
	void setEntityType(GmEntityType entityType);
	// @formatter:on

}
