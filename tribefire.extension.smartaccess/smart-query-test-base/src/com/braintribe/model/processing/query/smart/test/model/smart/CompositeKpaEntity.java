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
package com.braintribe.model.processing.query.smart.test.model.smart;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.accessA.CompositeKpaEntityA;

/**
 * Mapped to {@link CompositeKpaEntityA} using these mappings:
 * 
 * <ul>
 * <li>PersonA.compositeId = this.personId</li>
 * <li>PersonA.compositeName = this.personName</li>
 * <li>PersonA.compositeCompanyName = this.personCompanyName</li>
 * </ul>
 */
public interface CompositeKpaEntity extends StandardSmartIdentifiable {
	
	EntityType<CompositeKpaEntity> T = EntityTypes.T(CompositeKpaEntity.class);

	Long getPersonId();
	void setPersonId(Long personId);

	String getPersonName();
	void setPersonName(String personName);

	String getPersonCompanyName();
	void setPersonCompanyName(String personCompanyName);

	String getDescription();
	void setDescription(String description);

}
