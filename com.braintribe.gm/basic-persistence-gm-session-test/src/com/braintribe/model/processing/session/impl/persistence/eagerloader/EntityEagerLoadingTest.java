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

import static com.braintribe.utils.lcd.CollectionTools2.first;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.braintribe.model.processing.session.impl.persistence.eagerloader.model.EagerItem;
import com.braintribe.model.processing.session.impl.persistence.eagerloader.model.EagerOwner;

/**
 * @see AbstractEagerLoaderTest
 * 
 * @author peter.gazdik
 */
public class EntityEagerLoadingTest extends AbstractEagerLoaderTest {

	/**
	 * Test eager loading for a entity property, where every entity has a value for that property.
	 */
	@Test
	public void entity_Full() throws Exception {
		for (int i = 0; i < NUMBER_OF_OWNERS; i++)
			newOwner(i, o -> o.setEntity(newItemOf(o, 0)));

		loadOwners();

		for (EagerOwner owner : owners) {
			EagerItem entity = owner.getEntity();

			Assertions.assertThat(entity).isNotNull();
			Assertions.assertThat(entity.getName()).isEqualTo(getItemName(owner, 0));
		}

		assertJustOneQuery();
	}

	/**
	 * Test eager loading for a entity property, where every entity has a value for that property.
	 */
	@Test
	public void entity_Full_TripleSize() throws Exception {
		int nOwners = 2 * NUMBER_OF_OWNERS + (NUMBER_OF_OWNERS / 2);

		for (int i = 0; i < nOwners; i++)
			newOwner(i, o -> o.setEntity(newItemOf(o, 0)));

		loadOwners();

		first(owners).getEntity();
		Assertions.assertThat(queryCounter.totalCount).isEqualTo(3);

		for (EagerOwner owner : owners) {
			EagerItem entity = owner.getEntity();

			Assertions.assertThat(entity).isNotNull();
			Assertions.assertThat(entity.getName()).isEqualTo(getItemName(owner, 0));
		}
	}

}
