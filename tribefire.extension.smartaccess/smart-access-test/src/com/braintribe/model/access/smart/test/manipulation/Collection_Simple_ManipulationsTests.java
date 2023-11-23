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

import static com.braintribe.utils.lcd.CollectionTools2.asMap;
import static com.braintribe.utils.lcd.CollectionTools2.asSet;
import static com.braintribe.utils.lcd.CollectionTools2.newList;
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static com.braintribe.utils.lcd.CollectionTools2.newSet;
import static java.util.Arrays.asList;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class Collection_Simple_ManipulationsTests extends AbstractManipulationsTests {

	SmartPersonA sp;

	@Before
	public void prepareData() throws Exception {
		sp = newSmartPersonA();
		sp.setNameA("sp");
		commit();
		BtAssertions.assertThat(countPersonA()).isEqualTo(1);
	}

	// ####################################
	// ## . . . . Change Value . . . . . ##
	// ####################################

	@Test
	public void cangeSetValue() throws Exception {
		sp.setNickNamesSetA(asSet("nick1", "nick2"));
		commit();

		PersonA p = loadDelegate();

		BtAssertions.assertThat(p.getNickNamesSetA()).isNotEmpty().containsOnly("nick1", "nick2");
	}

	// ####################################
	// ## . . . . . . Insert . . . . . . ##
	// ####################################

	@Test
	public void insertToList() throws Exception {
		sp.setNickNamesListA(newList());
		sp.getNickNamesListA().add("nick1");
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getNickNamesListA()).isNotEmpty().containsOnly("nick1");
	}

	@Test
	public void insertToSet() throws Exception {
		sp.setNickNamesSetA(newSet());
		sp.getNickNamesSetA().add("nick1");
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getNickNamesSetA()).isNotEmpty().containsOnly("nick1");
	}

	@Test
	public void insertToMap() throws Exception {
		sp.setNickNamesMapA(newMap());
		sp.getNickNamesMapA().put(1, "nick1");
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getNickNamesMapA()).containsKeys(1).containsValues("nick1");
	}

	// ####################################
	// ## . . . . . Bulk Insert . . . . .##
	// ####################################

	@Test
	public void bulkInsertToList() throws Exception {
		sp.setNickNamesListA(newList());
		sp.getNickNamesListA().addAll(asList("nick1", "nick2", "nick3"));
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getNickNamesListA()).isNotEmpty().containsOnly("nick1", "nick2", "nick3");
	}

	@Test
	public void bulkInsertToSet() throws Exception {
		sp.setNickNamesMapA(newMap());
		sp.getNickNamesSetA().addAll(asList("nick1", "nick2", "nick3"));
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getNickNamesSetA()).isNotEmpty().containsOnly("nick1", "nick2", "nick3");
	}

	@Test
	public void bulkInsertToMap() throws Exception {
		sp.setNickNamesMapA(newMap());
		sp.getNickNamesMapA().putAll(asMap(1, "nick1", 2, "nick2", 3, "nick3"));
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getNickNamesMapA()).containsKeys(1, 2, 3).containsValues("nick1", "nick2", "nick3");
	}

	// ####################################
	// ## . . . . . . Remove . . . . . . ##
	// ####################################

	@Test
	public void removeFromList() throws Exception {
		insertToList();

		sp.getNickNamesListA().remove(0);
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getNickNamesListA()).isEmpty();
	}

	@Test
	public void removeFromSet() throws Exception {
		insertToSet();

		sp.getNickNamesSetA().remove("nick1");
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getNickNamesSetA()).isEmpty();
	}

	@Test
	public void removeFromMap() throws Exception {
		insertToMap();

		sp.getNickNamesMapA().remove(1);
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getNickNamesMapA()).isEmpty();
	}

	// ####################################
	// ## . . . . . Bulk Remove. . . . . ##
	// ####################################

	@Test
	public void bulkRemoveFromList() throws Exception {
		bulkInsertToList();

		sp.getNickNamesListA().removeAll(asList("nick1", "nick2", "nick3"));
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getNickNamesListA()).isEmpty();
	}

	@Test
	public void bulkRemoveFromSet() throws Exception {
		bulkInsertToSet();

		sp.getNickNamesSetA().removeAll(asList("nick1", "nick2", "nick3"));
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getNickNamesSetA()).isEmpty();
	}

	@Test
	public void bulkRemoveFromMap() throws Exception {
		bulkInsertToMap();

		sp.getNickNamesMapA().keySet().removeAll(asList(1, 2, 3));
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getNickNamesMapA()).isEmpty();
	}

	// ####################################
	// ## . . . . . . Clear . . . . . . .##
	// ####################################

	@Test
	public void clearCollection() throws Exception {
		bulkInsertToSet();

		sp.getNickNamesSetA().clear();
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getNickNamesSetA()).isEmpty();
	}

	private PersonA loadDelegate() {
		return personAByName("sp");
	}

}
