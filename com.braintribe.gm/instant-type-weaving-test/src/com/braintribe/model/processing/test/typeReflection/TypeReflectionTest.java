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

import static org.fest.assertions.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.GMF;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.reflection.GenericModelTypeReflection;

/**
 * @author peter.gazdik
 */
public class TypeReflectionTest {

	static final GenericModelTypeReflection typeReflection = GMF.getTypeReflection();
	static final String GE_SIGNATURE = GenericEntity.class.getName();
	static final String WRONG_SIGNATURE = "non-existent-entity";

	@Test
	public void findsEntityByName() {
		typeReflection.getType(GE_SIGNATURE);
	}

	@Test
	public void findsCollectionByName() {
		typeReflection.getType(String.format("list<%s>", GE_SIGNATURE));
		typeReflection.getType(String.format("set<%s>", GE_SIGNATURE));
		typeReflection.getType(String.format("map<%s,%s>", GE_SIGNATURE, GE_SIGNATURE));
	}

	@Test
	public void returnsNullWhenNotExists() {
		assertThat(findType(WRONG_SIGNATURE)).isNull();

		assertThat(findType(String.format("list<%s>", WRONG_SIGNATURE))).isNull();
		assertThat(findType(String.format("set<%s>", WRONG_SIGNATURE))).isNull();
		assertThat(findType(String.format("map<%s,%s>", WRONG_SIGNATURE, WRONG_SIGNATURE))).isNull();
	}

	private Object findType(String typeSignature) {
		return typeReflection.findType(typeSignature);
	}

}
