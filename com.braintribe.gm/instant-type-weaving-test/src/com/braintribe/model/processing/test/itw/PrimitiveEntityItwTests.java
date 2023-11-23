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
package com.braintribe.model.processing.test.itw;

import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.ImportantItwTestSuperType;
import com.braintribe.model.processing.test.itw.entity.PrimitivePropsEntity;

/**
 * 
 */
public class PrimitiveEntityItwTests extends ImportantItwTestSuperType {

	@Test
	public void testSimpleEntity() {
		PrimitivePropsEntity entity = instantiate(PrimitivePropsEntity.class);

		entity.getIntValue();
		entity.getLongValue();
		entity.getFloatValue();
		entity.getDoubleValue();
		entity.getBooleanValue();
	}

	protected <T extends GenericEntity> T instantiate(Class<T> beanClass) {
		EntityType<T> entityType = typeReflection().getEntityType(beanClass);
		return entityType.create();
	}

	protected <T extends GenericEntity> T instantiatePlain(Class<T> beanClass) {
		EntityType<T> entityType = typeReflection().getEntityType(beanClass);
		return entityType.createPlain();
	}

	private static GenericModelTypeReflection typeReflection() {
		return GMF.getTypeReflection();
	}

}
