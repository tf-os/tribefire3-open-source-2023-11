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
package com.braintribe.model.processing.security.manipulation;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.ToStringInformation;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.generic.value.EntityReference;

/**
 * An entry that describes some security violation reported by a {@link ManipulationSecurityExpert}
 */
@ToStringInformation("${description}")
public interface SecurityViolationEntry extends GenericEntity {
	
	final EntityType<SecurityViolationEntry> T = EntityTypes.T(SecurityViolationEntry.class);

	// @formatter:off
	AtomicManipulation getCausingManipulation();
	void setCausingManipulation(AtomicManipulation causingManipulation);

	EntityReference getEntityReference();
	void setEntityReference(EntityReference entityReference);

	String getPropertyName();
	void setPropertyName(String propertyName);

	String getDescription();
	void setDescription(String description);
	// @formatter:on

}
