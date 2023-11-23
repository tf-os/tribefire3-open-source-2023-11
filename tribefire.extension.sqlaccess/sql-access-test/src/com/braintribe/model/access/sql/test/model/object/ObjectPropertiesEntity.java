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
package com.braintribe.model.access.sql.test.model.object;

import java.util.Set;

import com.braintribe.model.access.sql.test.model.SqlAccessEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;

/**
 * @author peter.gazdik
 */
public interface ObjectPropertiesEntity extends SqlAccessEntity {

	EntityType<ObjectPropertiesEntity> T = EntityTypes.T(ObjectPropertiesEntity.class);

	Object getValue();
	void setValue(Object value);

	Object getValue2();
	void setValue2(Object value2);

	Set<Object> getValueSet();
	void setValueSet(Set<Object> valueSet);

}
