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
package com.braintribe.model.access.smart.test.manipulation;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.first;
import static com.braintribe.utils.lcd.CollectionTools2.isEmpty;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.processing.query.fluent.SelectQueryBuilder;
import com.braintribe.model.processing.query.smart.test.model.accessA.Id2UniqueEntityA;
import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonId2UniqueEntityLink;
import com.braintribe.model.processing.query.smart.test.model.smart.Id2UniqueEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.query.SelectQuery;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class Id2Unique_ManipulationsTests extends AbstractManipulationsTests {

	private SmartPersonA sp;
	private Id2UniqueEntity su1, su2;

	@Before
	public void prepareData() throws Exception {
		sp = newSmartPersonA();
		sp.setNameA("sp");

		su1 = newId2UniqueEntity();
		su1.setId("u1");
		su2 = newId2UniqueEntity();
		su2.setId("u2");

		commit();

		BtAssertions.assertThat(countPersonA()).isEqualTo(1);
		BtAssertions.assertThat(countId2UniqueEntityA()).isEqualTo(2);
	}

	// ####################################
	// ## . . . Change - Entity . . . . .##
	// ####################################

	@Test
	public void changeEmptyEntityValue() throws Exception {
		sp.setId2UniqueEntityA(su1);
		commit();

		PersonA pA = personAByName("sp");
		BtAssertions.assertThat(pA).isNotNull();
		BtAssertions.assertThat(pA.getId2UniqueEntityA()).isNotNull();
		BtAssertions.assertThat(pA.getId2UniqueEntityA().getUnique()).isEqualTo(su1.getId());
	}

	@Test
	public void addToCollection() throws Exception {
		// ChangeValue
		sp.setId2UniqueEntitySetA(asSet(su1));
		commit();

		// Add
		sp.getId2UniqueEntitySetA().add(su2);
		commit();

		PersonA pA = personAByName("sp");
		BtAssertions.assertThat(pA).isNotNull();
		BtAssertions.assertThat(pA.getId2UniqueEntitySetA()).hasSize(2);
	}

	@Test
	public void setLink() throws Exception {
		sp.setLinkId2UniqueEntityA(su1);
		commit();

		String linkedUnique = loadLinkUnique();
		BtAssertions.assertThat(linkedUnique).isEqualTo(su1.getId());
	}

	// ####################################
	// ## . . . . . Helpers . . . . . . .##
	// ####################################

	protected long countId2UniqueEntityA() {
		return count(Id2UniqueEntityA.class, smoodA);
	}

	private String loadLinkUnique() {
		List<PersonId2UniqueEntityLink> list = listAllByProperty(PersonId2UniqueEntityLink.class, "personName", sp.getNameA());

		if (list != null && list.size() > 1) {
			Assert.fail("Cannot have more than 1 value for PerstonItemLink. This link represents an entity property.");
		}

		return isEmpty(list) ? null : first(list).getLinkUnique();
	}

	private <T extends GenericEntity> List<T> listAllByProperty(Class<T> clazz, String propertyName, Object propertyValue) {
		SelectQuery query = new SelectQueryBuilder().from(clazz, "e").select("e").where().property("e", propertyName).eq(propertyValue)
				.done();

		try {
			return cast(smoodB.query(query).getResults());

		} catch (Exception e) {
			throw new RuntimeException("Query evaluation failed for: " + smoodB, e);
		}
	}
}
