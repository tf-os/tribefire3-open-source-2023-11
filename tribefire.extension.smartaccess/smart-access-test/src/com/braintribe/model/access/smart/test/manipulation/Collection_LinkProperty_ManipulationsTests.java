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
import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemLink;
import com.braintribe.model.processing.query.smart.test.model.accessB.PersonItemSetLink;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartItem;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class Collection_LinkProperty_ManipulationsTests extends AbstractManipulationsTests {

	private SmartPersonA sp;
	private SmartItem si1, si2, si3, si4;
	private String itemName;
	private final Set<String> itemNames = newSet();

	@Before
	public void prepareData() throws Exception {
		sp = newSmartPersonA();
		sp.setNameA("sp");

		si1 = newSmartItem();
		si1.setNameB("si1");
		si2 = newSmartItem();
		si2.setNameB("si2");
		si3 = newSmartItem();
		si3.setNameB("si3");
		si4 = newSmartItem();
		si4.setNameB("si4");

		commit();

		BtAssertions.assertThat(countPersonA()).isEqualTo(1);
		BtAssertions.assertThat(countItemB()).isEqualTo(4);
	}

	// ####################################
	// ## . . . Change - Entity . . . . .##
	// ####################################

	@Test
	public void changeEmptyEntityValue() throws Exception {
		sp.setLinkItem(si1);
		commit();

		loadItemName();

		BtAssertions.assertThat(itemName).isEqualTo(si1.getNameB());
	}

	@Test
	public void changeExistingEntityValue() throws Exception {
		sp.setLinkItem(si1);
		commit();

		sp.setLinkItem(si2);
		commit();

		loadItemName();

		BtAssertions.assertThat(itemName).isEqualTo(si2.getNameB());
	}

	@Test
	public void nullifyExistingEntityValue() throws Exception {
		sp.setLinkItem(si1);
		commit();

		sp.setLinkItem(null);
		commit();

		loadItemName();

		BtAssertions.assertThat(itemName).isNull();
	}

	// ####################################
	// ## . . . Change - Collection . . .##
	// ####################################

	@Test
	public void changeEmptySetValue() throws Exception {
		sp.setLinkItems(asSet(si1, si2));
		commit();

		loadItemNames();

		BtAssertions.assertThat(itemNames).isNotEmpty().hasSize(2).containsOnly("si1", "si2");
	}

	@Test
	public void changeNonEmptySetValue() throws Exception {
		sp.setLinkItems(asSet(si1, si2, si3));
		commit();

		sp.setLinkItems(asSet(si3, si4));
		commit();

		loadItemNames();

		BtAssertions.assertThat(itemNames).isNotEmpty().hasSize(2).containsOnly("si3", "si4");
	}

	// ####################################
	// ## . . . . . . Insert . . . . . . ##
	// ####################################

	@Test
	public void insert() throws Exception {
		sp.setLinkItems(new HashSet<SmartItem>());
		sp.getLinkItems().add(si1);
		commit();

		loadItemNames();

		BtAssertions.assertThat(itemNames).isNotEmpty().hasSize(1).containsOnly("si1");
	}

	@Test
	public void bulkInsert() throws Exception {
		sp.setLinkItems(new HashSet<SmartItem>());
		sp.getLinkItems().addAll(asSet(si1, si2, si3));
		commit();

		loadItemNames();

		BtAssertions.assertThat(itemNames).isNotEmpty().hasSize(3).containsOnly("si1", "si2", "si3");
	}

	@Test
	public void inserWithNewEntities() throws Exception {
		SmartPersonA newSp = newSmartPersonA();
		newSp.setNameA("newSp");

		SmartItem newSi1 = newSmartItem();
		newSi1.setNameB("newSi1");

		SmartItem newSi2 = newSmartItem();
		newSi2.setNameB("newSi2");

		newSp.setLinkItems(new HashSet<SmartItem>());
		newSp.getLinkItems().addAll(asSet(newSi1, newSi2));
		commit();

		// loadItemNames
		Set<String> newItemNames = newSet();
		for (PersonItemSetLink link: listAllByProperty(PersonItemSetLink.class, "personName", newSp.getNameA(), smoodB)) {
			newItemNames.add(link.getItemName());
		}

		BtAssertions.assertThat(newItemNames).isNotEmpty().hasSize(2).containsOnly("newSi1", "newSi2");
	}

	// ####################################
	// ## . . . . . . Remove . . . . . . ##
	// ####################################

	@Test
	public void remove() throws Exception {
		insert();

		sp.getLinkItems().remove(si1);
		commit();

		loadItemNames();

		BtAssertions.assertThat(itemNames).isEmpty();
	}

	@Test
	public void bulkRemove() throws Exception {
		bulkInsert();

		sp.getLinkItems().removeAll(asSet(si2, si3));
		commit();

		loadItemNames();

		BtAssertions.assertThat(itemNames).hasSize(1).containsOnly("si1");
	}

	@Test
	public void clear() throws Exception {
		bulkInsert();

		sp.getLinkItems().clear();
		commit();

		loadItemNames();

		BtAssertions.assertThat(itemNames).isEmpty();
	}

	// ####################################
	// ## . . . . . Helpers . . . . . . .##
	// ####################################

	private void loadItemName() {
		List<PersonItemLink> list = listAllByProperty(PersonItemLink.class, "personName", sp.getNameA(), smoodB);

		if (list != null && list.size() > 1) {
			Assert.fail("Cannot have more than 1 value for PerstonItemLink. This link represents an entity property.");
		}

		itemName = isEmpty(list) ? null : first(list).getItemName();
	}

	private void loadItemNames() {
		itemNames.clear();

		for (PersonItemSetLink link: listAllByProperty(PersonItemSetLink.class, "personName", sp.getNameA(), smoodB)) {
			itemNames.add(link.getItemName());
		}
	}

}
