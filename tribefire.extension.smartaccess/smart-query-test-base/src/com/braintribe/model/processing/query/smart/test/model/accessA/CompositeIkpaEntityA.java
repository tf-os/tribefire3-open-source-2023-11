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
import com.braintribe.model.processing.query.smart.test.model.smart.CompositeIkpaEntity;

/**
 * @see CompositeIkpaEntity
 */

public interface CompositeIkpaEntityA extends StandardIdentifiableA {

	final EntityType<CompositeIkpaEntityA> T = EntityTypes.T(CompositeIkpaEntityA.class);

	// @formatter:off
	Long getPersonId();
	void setPersonId(Long personId);

	String getPersonName();
	void setPersonName(String personName);

	Long getPersonId_Set();
	void setPersonId_Set(Long personId_Set);

	String getPersonName_Set();
	void setPersonName_Set(String personName_Set);

	String getDescription();
	void setDescription(String description);
	// @formatter:on

}
