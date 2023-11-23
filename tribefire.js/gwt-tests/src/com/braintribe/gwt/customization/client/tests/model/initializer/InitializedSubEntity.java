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
package com.braintribe.gwt.customization.client.tests.model.initializer;

import java.util.Date;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

public interface InitializedSubEntity extends InitializedEntity {

	EntityType<InitializedSubEntity> T = EntityTypes.T(InitializedSubEntity.class);

	// override value with explicit one
	@Override
	@Initializer("88")
	int getIntValue();
	@Override
	void setIntValue(int intValue);

	// override with default (0)
	@Override
	@Initializer("0L")
	long getLongValue();
	@Override
	void setLongValue(long longValue);

	// re-declared does not change the default
	@Override
	boolean getBooleanValue();
	@Override
	void setBooleanValue(boolean doubleValue);

	@Override
	@Initializer("null")
	Date getDateValue();
	@Override
	void setDateValue(Date value);

	long getNewLongValue();
	void setNewLongValue(long value);

}
