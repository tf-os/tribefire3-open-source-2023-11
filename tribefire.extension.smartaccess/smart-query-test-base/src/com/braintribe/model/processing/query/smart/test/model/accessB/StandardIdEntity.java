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
package com.braintribe.model.processing.query.smart.test.model.accessB;

import java.util.Set;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.EntityTypes;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartStringIdEntity;

/**
 * @see SmartStringIdEntity
 */

public interface StandardIdEntity extends StandardIdentifiableB {

	final EntityType<StandardIdEntity> T = EntityTypes.T(StandardIdEntity.class);

	// @formatter:off
	String getName();
	void setName(String name);

	StandardIdEntity getParent();
	void setParent(StandardIdEntity parent);

	Set<StandardIdEntity> getChildren();
	void setChildren(Set<StandardIdEntity> children);
	
	Long getKpaParentId();
	void setKpaParentId(Long value);
	// @formatter:on

}
