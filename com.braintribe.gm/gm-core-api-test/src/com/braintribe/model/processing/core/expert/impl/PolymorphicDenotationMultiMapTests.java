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
package com.braintribe.model.processing.core.expert.impl;

import static com.braintribe.testing.junit.assertions.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationComment;
import com.braintribe.model.generic.reflection.EntityType;

/**
 * Tests for {@link PolymorphicDenotationMultiMap}
 */
public class PolymorphicDenotationMultiMapTests {

	private final PolymorphicDenotationMultiMap<GenericEntity, String> map = new PolymorphicDenotationMultiMap<>();

	@Test
	public void emptyMap() throws Exception {
		assertConfigSize(0);

		assertThat(map.<String> find(ManipulationComment.T)).isNull();
		assertSizeFor(ManipulationComment.T, 0);
		assertSizeFor(Manipulation.T, 0);
	}

	@Test
	public void singleElement() throws Exception {
		map.put(Manipulation.T, "Manipulation");

		assertConfigSize(1);

		assertSizeFor(ManipulationComment.T, 1);
		assertSizeFor(Manipulation.T, 1);
		assertSizeFor(GenericEntity.T, 0);
	}

	@Test
	public void multipleElements() throws Exception {
		map.put(ManipulationComment.T, "ManipulationComment");
		map.put(Manipulation.T, "Manipulation1");
		map.put(Manipulation.T, "Manipulation2");
		map.put(GenericEntity.T, "GenericEntity");

		assertConfigSize(4);

		assertSizeFor(ManipulationComment.T, 4);
		assertSizeFor(Manipulation.T, 3);
		assertSizeFor(GenericEntity.T, 1);
	}

	@Test
	public void removeWorks() throws Exception {
		map.put(ManipulationComment.T, "ManipulationComment 1");
		map.put(ManipulationComment.T, "ManipulationComment 2");
		map.put(ManipulationComment.T, "ManipulationComment 3");
		map.put(Manipulation.T, "Manipulation");

		assertConfigSize(4);
		assertSizeFor(ManipulationComment.T, 4);

		map.removeEntry(ManipulationComment.T, "Non-Existent Value");
		assertConfigSize(4);
		assertSizeFor(ManipulationComment.T, 4);

		map.removeEntry(ManipulationComment.T, "ManipulationComment 1");
		assertConfigSize(3);
		assertSizeFor(ManipulationComment.T, 3);

		// removes remaining 2 values
		map.remove(ManipulationComment.T);
		assertConfigSize(1);
		assertSizeFor(ManipulationComment.T, 1);

	}

	private void assertSizeFor(EntityType<?> type, int size) {
		assertThat(map.findAll(type)).hasSize(size);
	}

	private void assertConfigSize(int size) {
		assertThat(map.configurationSize()).isEqualTo(size);
	}

}
