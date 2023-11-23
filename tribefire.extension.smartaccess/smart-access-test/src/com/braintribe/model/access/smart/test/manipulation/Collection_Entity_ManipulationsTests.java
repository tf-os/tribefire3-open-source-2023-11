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
import static com.braintribe.utils.lcd.CollectionTools2.newMap;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class Collection_Entity_ManipulationsTests extends AbstractManipulationsTests {

	SmartPersonA sp;
	Company c1, c2;

	@Before
	public void prepareData() throws Exception {
		sp = newSmartPersonA();
		sp.setNameA("sp");

		c2 = newCompany();
		c2.setNameA("c2");

		c1 = newCompany();
		c1.setNameA("c1");

		commit();

		BtAssertions.assertThat(countPersonA()).isEqualTo(1);
		BtAssertions.assertThat(countCompanyA()).isEqualTo(2);
	}

	// ####################################
	// ## . . . . Change Value . . . . . ##
	// ####################################

	@Test
	public void changeSetValue() throws Exception {
		sp.setCompanySetA(asSet(c1, c2));
		commit();

		PersonA p = loadDelegate();

		BtAssertions.assertThat(p.getCompanySetA()).isNotEmpty().hasSize(2);
	}

	// ####################################
	// ## . . . . . . Insert . . . . . . ##
	// ####################################

	@Test
	public void insertToList() throws Exception {
		sp.setCompanyListA(new ArrayList<Company>());
		sp.getCompanyListA().add(c1);
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getCompanyListA()).isNotEmpty().hasSize(1);
	}

	@Test
	public void insertToSet() throws Exception {
		sp.setCompanySetA(new HashSet<Company>());
		sp.getCompanySetA().add(c1);
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getCompanySetA()).isNotEmpty().hasSize(1);
	}

	@Test
	public void insertToMap() throws Exception {
		sp.setCompanyOwnerA(new HashMap<Company, SmartPersonA>());
		sp.getCompanyOwnerA().put(c1, sp);
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getCompanyOwnerA()).isNotEmpty().hasSize(1);
	}

	// ####################################
	// ## . . . . . Bulk Insert . . . . .##
	// ####################################

	@Test
	public void bulkInsertToList() throws Exception {
		sp.setCompanyListA(new ArrayList<Company>());
		sp.getCompanyListA().addAll(asList(c1, c2));
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getCompanyListA()).isNotEmpty().hasSize(2);
	}

	@Test
	public void bulkInsertToSet() throws Exception {
		sp.setCompanySetA(new HashSet<Company>());
		sp.getCompanySetA().addAll(asList(c1, c2));
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getCompanySetA()).isNotEmpty().hasSize(2);
	}

	@Test
	public void bulkInsertToMap() throws Exception {
		sp.setCompanyOwnerA(newMap());
		sp.getCompanyOwnerA().putAll(asMap(c1, sp, c2, sp));
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getCompanyOwnerA()).hasSize(2);
	}

	// ####################################
	// ## . . . . . . Remove . . . . . . ##
	// ####################################

	@Test
	public void removeFromList() throws Exception {
		insertToList();

		sp.getCompanyListA().remove(0);
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getCompanyListA()).isEmpty();
	}

	@Test
	public void removeFromSet() throws Exception {
		insertToSet();

		sp.getCompanySetA().remove(c1);
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getCompanySetA()).isEmpty();
	}

	@Test
	public void removeFromMap() throws Exception {
		insertToMap();

		sp.getCompanyOwnerA().remove(c1);
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getCompanyOwnerA()).isEmpty();
	}

	// ####################################
	// ## . . . . . Bulk Remove. . . . . ##
	// ####################################

	@Test
	public void bulkRemoveFromList() throws Exception {
		bulkInsertToList();

		sp.getCompanyListA().removeAll(asList(c1, c2));
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getCompanyListA()).isEmpty();
	}

	@Test
	public void bulkRemoveFromSet() throws Exception {
		bulkInsertToSet();

		sp.getCompanySetA().removeAll(asList(c1, c2));
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getCompanySetA()).isEmpty();
	}

	@Test
	public void bulkRemoveFromMap() throws Exception {
		bulkInsertToMap();

		sp.getCompanyOwnerA().keySet().removeAll(asList(c1, c2));
		commit();

		PersonA p = loadDelegate();
		BtAssertions.assertThat(p.getCompanyOwnerA()).isEmpty();
	}

	private PersonA loadDelegate() {
		return personAByName("sp");
	}

}
