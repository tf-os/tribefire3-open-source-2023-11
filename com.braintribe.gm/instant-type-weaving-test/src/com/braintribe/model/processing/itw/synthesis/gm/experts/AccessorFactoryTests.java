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
package com.braintribe.model.processing.itw.synthesis.gm.experts;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.junit.Test;

import com.braintribe.model.processing.ImportantItwTestSuperType;

/**
 * 
 */
public class AccessorFactoryTests extends ImportantItwTestSuperType {

	@Test
	public void reflectionAccess() {
		ThePerson p = new ThePerson();

		BiConsumer<ThePerson, Object> setter = AccessorFactory.setter(ThePerson.class, "setName", String.class);
		Function<ThePerson, Object> getter = AccessorFactory.getter(ThePerson.class, "getName", String.class);

		setter.accept(p, "ONE");
		assertThat(p.getName()).isEqualTo("ONE");

		p.setName("TWO");
		assertThat(getter.apply(p)).isEqualTo("TWO");
	}

	class ThePerson {
		String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}

}
