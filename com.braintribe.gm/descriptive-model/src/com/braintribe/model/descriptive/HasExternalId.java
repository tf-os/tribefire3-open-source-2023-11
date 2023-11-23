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
package com.braintribe.model.descriptive;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.annotation.Abstract;
import com.braintribe.model.generic.annotation.SelectiveInformation;
import com.braintribe.model.generic.annotation.meta.Mandatory;
import com.braintribe.model.generic.annotation.meta.Unique;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

@SelectiveInformation("${externalId}")
@Abstract
public interface HasExternalId extends GenericEntity {

	EntityType<HasExternalId> T = EntityTypes.T(HasExternalId.class);

	String externalId = "externalId";

	@Mandatory
	@Unique
	String getExternalId();
	void setExternalId(String externalId);

	/**
	 * Prints a simple, user-friendly descriptor of this instance, consisting at least of short type and externalId information, for example:
	 * {@code HibernateAccess[custom.access.id]}.
	 */
	default String simpleIdentification() {
		return entityType().getShortName() + "[" + getExternalId() + "]";
	}

}
