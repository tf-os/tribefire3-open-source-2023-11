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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.processing.ImportantItwTestSuperType;
import com.braintribe.model.processing.test.itw.entity.Color;

/**
 * Tests for methods of {@link EntityType}
 */
public class EnumTypeTests extends ImportantItwTestSuperType {

	@Test
	public void getEnumValue() {
		assertThat(Color.T.getEnumValue("red")).isSameAs(Color.red);
	}

	@Test
	public void assignability() {
		assertThat(Color.T.isAssignableFrom(Color.T)).isTrue();
	}

	@Test
	public void instanceofChecking() {
		assertThat(Color.T.isInstance("string")).isFalse();
		assertThat(Color.T.isInstance(Color.red)).isTrue();
	}

}
