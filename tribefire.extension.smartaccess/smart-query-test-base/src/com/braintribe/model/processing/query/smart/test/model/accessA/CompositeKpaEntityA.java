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
package com.braintribe.model.processing.query.smart.test.model.accessA;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.smart.CompositeKpaEntity;

/**
 * @see CompositeKpaEntity
 */

public interface CompositeKpaEntityA extends StandardIdentifiableA {

	final EntityType<CompositeKpaEntityA> T = EntityTypes.T(CompositeKpaEntityA.class);

	// @formatter:off
	Long getPersonId();
	void setPersonId(Long personId);

	String getPersonName();
	void setPersonName(String personName);

	String getPersonCompanyName();
	void setPersonCompanyName(String personCompanyName);

	String getDescription();
	void setDescription(String description);
	// @formatter:on

}
