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
package com.braintribe.model.processing.session.impl.persistence.eagerloader;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Set;
import java.util.stream.Collectors;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.model.processing.session.impl.persistence.eagerloader.model.EagerItem;
import com.braintribe.model.processing.session.impl.persistence.eagerloader.model.EagerOwner;

/**
 * @see AbstractEagerLoaderTest
 * 
 * @author peter.gazdik
 */
public class SetEagerLoadingTest extends AbstractEagerLoaderTest {

	/**
	 * Test eager loading for a {@code Set<String>} property, where every entity has a value for that property.
	 */
	@Test
	public void simpleSet_Full() throws Exception {
		for (int i = 0; i < NUMBER_OF_OWNERS; i++)
			newOwner(i, o -> o.setStringSet(simpleSetFor(o)));

		loadOwners();

		for (EagerOwner owner : owners) {
			Set<String> stringSet = owner.getStringSet();
			Assertions.assertThat(stringSet).isEqualTo(simpleSetFor(owner));
		}

		assertJustOneQuery();
	}

	private Set<String> simpleSetFor(EagerOwner owner) {
		Integer id = owner.getId();
		return asSet("X_" + id, "Y_" + id, "Z_" + id);
	}

	/**
	 * Test eager loading for a {@code Set<String>} property, where for some entities the property is empty.
	 */
	@Test
	public void simpleSet_Incomplete() throws Exception {
		for (int i = 0; i < NUMBER_OF_OWNERS; i++)
			newOwner(i, o -> o.setStringSet(simpleSetIfEvenId(o)));

		loadOwners();

		for (EagerOwner owner : owners) {
			Set<String> stringSet = owner.getStringSet();
			Assertions.assertThat(stringSet).isEqualTo(simpleSetIfEvenId(owner));
		}

		assertJustOneQuery();
	}

	private Set<String> simpleSetIfEvenId(EagerOwner owner) {
		int id = (Integer) owner.getId();

		return id % 2 == 1 ? newSet() : simpleSetFor(owner);
	}

	/**
	 * Test eager loading for a {@code Set<GenericEntity>} property, where every entity has a value for that property.
	 */
	@Test
	public void entitySet() throws Exception {
		for (int i = 0; i < NUMBER_OF_OWNERS; i++)
			newOwner(i, o -> o.setEntitySet(itemsSetFor(o)));

		loadOwners();

		for (EagerOwner owner : owners) {
			Set<EagerItem> entitySet = owner.getEntitySet();

			Assertions.assertThat(entitySet).hasSize(3);

			Set<String> itemNames = entitySet.stream().map(EagerItem::getName).collect(Collectors.toSet());
			Assertions.assertThat(itemNames).isEqualTo(//
					asSet(getItemName(owner, 0), getItemName(owner, 1), getItemName(owner, 2)) //
			);
		}

		assertJustOneQuery();
	}

	private Set<EagerItem> itemsSetFor(EagerOwner owner) {
		return asSet(newItemOf(owner, 0), newItemOf(owner, 1), newItemOf(owner, 2));
	}

}
