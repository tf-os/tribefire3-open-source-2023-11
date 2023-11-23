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

import static com.braintribe.utils.lcd.CollectionTools2.asList;
import static com.braintribe.utils.lcd.CollectionTools2.newList;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.model.processing.session.impl.persistence.eagerloader.model.EagerItem;
import com.braintribe.model.processing.session.impl.persistence.eagerloader.model.EagerOwner;

/**
 * @see AbstractEagerLoaderTest
 * 
 * @author peter.gazdik
 */
public class ListEagerLoadingTest extends AbstractEagerLoaderTest {

	/**
	 * Test eager loading for a {@code List<String>} property, where every entity has a value for that property.
	 */
	@Test
	public void simpleList_Full() throws Exception {
		for (int i = 0; i < NUMBER_OF_OWNERS; i++)
			newOwner(i, o -> o.setStringList(simpleListFor(o)));

		loadOwners();

		for (EagerOwner owner : owners) {
			List<String> stringList = owner.getStringList();
			Assertions.assertThat(stringList).isEqualTo(simpleListFor(owner));
		}

		assertJustOneQuery();
	}

	private List<String> simpleListFor(EagerOwner owner) {
		Integer id = owner.getId();
		return asList("X_" + id, "Y_" + id, "Z_" + id);
	}

	/**
	 * Test eager loading for a {@code List<String>} property, where for some entities the property is empty.
	 */
	@Test
	public void simpleList_Incomplete() throws Exception {
		for (int i = 0; i < NUMBER_OF_OWNERS; i++)
			newOwner(i, o -> o.setStringList(simpleListIfEvenId(o)));

		loadOwners();

		for (EagerOwner owner : owners) {
			List<String> stringList = owner.getStringList();
			Assertions.assertThat(stringList).isEqualTo(simpleListIfEvenId(owner));
		}

		assertJustOneQuery();
	}

	private List<String> simpleListIfEvenId(EagerOwner owner) {
		int id = (Integer) owner.getId();

		return id % 2 == 1 ? newList() : simpleListFor(owner);
	}

	/**
	 * Test eager loading for a {@code List<GenericEntity>} property, where every entity has a value for that property.
	 */
	@Test
	public void entityList() throws Exception {
		for (int i = 0; i < NUMBER_OF_OWNERS; i++)
			newOwner(i, o -> o.setEntityList(itemsListFor(o)));

		loadOwners();

		for (EagerOwner owner : owners) {
			List<EagerItem> entityList = owner.getEntityList();

			Assertions.assertThat(entityList).hasSize(3);
			for (int i = 0; i < 3; i++)
				Assertions.assertThat(entityList.get(i).getName()).isEqualTo(getItemName(owner, i));
		}

		assertJustOneQuery();
	}

	private List<EagerItem> itemsListFor(EagerOwner owner) {
		return asList(newItemOf(owner, 0), newItemOf(owner, 1), newItemOf(owner, 2));
	}

}
