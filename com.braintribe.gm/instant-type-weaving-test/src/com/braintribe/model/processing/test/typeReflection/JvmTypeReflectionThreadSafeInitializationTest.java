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
package com.braintribe.model.processing.test.typeReflection;

import org.junit.Rule;
import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;
import com.braintribe.model.processing.test.itw.entity.AnotherTestEntity;
import com.braintribe.utils.junit.core.rules.ConcurrentRule;

public class JvmTypeReflectionThreadSafeInitializationTest {

	@Rule
	public ConcurrentRule concurrentRule = new ConcurrentRule(10);

	static GenericModelTypeReflection typeReflection = GMF.getTypeReflection();

	@Test
	public void testConcurrent2() {
		typeReflection.getEntityType(AnotherTestEntity.class);
	}

}
