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
package com.braintribe.testing.model.test.technical.features;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.Test;

import com.braintribe.model.generic.annotation.Initializer;
import com.braintribe.testing.test.AbstractTest;

/**
 * Tests {@link Initializer}s, i.e. default values, using entity {@link InitializerEntity}.
 *
 * @author michael.lafite
 */
public class InitializerEntityTest extends AbstractTest {

	@Test
	public void test() {
		InitializerEntity entity = InitializerEntity.T.create();

		assertThat(entity.getEnumProperty()).isEqualTo(SimpleEnum.TWO);
		assertThat(entity.getStringProperty()).isEqualTo("abc");
		assertThat(entity.getBooleanProperty()).isTrue();
		assertThat(entity.getIntegerProperty()).isEqualTo(123);
		assertThat(entity.getLongProperty()).isEqualTo(123);
		assertThat(entity.getFloatProperty()).isEqualTo(123.45f);
		assertThat(entity.getDoubleProperty()).isEqualTo(123.45d);
		assertThat(entity.getDecimalProperty()).isEqualTo(new BigDecimal("123.45"));
		assertThat(entity.getDateProperty()).isBefore(new Date());

		assertThat(entity.getPrimitiveBooleanProperty()).isTrue();
		assertThat(entity.getPrimitiveIntegerProperty()).isEqualTo(123);
		assertThat(entity.getPrimitiveLongProperty()).isEqualTo(123);
		assertThat(entity.getPrimitiveFloatProperty()).isEqualTo(123.45f);
		assertThat(entity.getPrimitiveDoubleProperty()).isEqualTo(123.45d);
	}

}
