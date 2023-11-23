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

import java.util.Arrays;
import java.util.HashSet;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class KeyProperty_ManipulationsTests extends AbstractManipulationsTests {

	@Test
	public void changeEntityValue_BothPreliminary() throws Exception {
		SmartPersonA p = newSmartPersonA();
		p.setNameA("p1");

		Company c = newCompany();
		c.setNameA("c1");

		// here we set the key-property value to our preliminary instance
		p.setKeyCompanyA(c);

		commit();

		PersonA personA = personAByName("p1");
		BtAssertions.assertThat(personA.getCompanyNameA()).isEqualTo("c1");
	}

	@Test
	public void changeEntityValue_KeyEntityPersistent() throws Exception {
		Company c = newCompany();
		c.setNameA("c1");
		commit();

		SmartPersonA p = newSmartPersonA();
		p.setNameA("p1");

		// here we set the key-property value to our persistent instance
		p.setKeyCompanyA(c);

		commit();

		PersonA personA = personAByName("p1");
		BtAssertions.assertThat(personA.getCompanyNameA()).isEqualTo("c1");
	}

	@Test
	public void changeEntityValue_BothPersistent() throws Exception {
		Company c = newCompany();
		c.setNameA("c1");
		commit();

		SmartPersonA p = newSmartPersonA();
		p.setNameA("p1");
		commit();

		// here we set the key-property value to our persistent instance
		p.setKeyCompanyA(c);

		commit();

		PersonA personA = personAByName("p1");
		BtAssertions.assertThat(personA.getCompanyNameA()).isEqualTo("c1");
	}

	@Test
	public void insertToCollection_BothPreliminary() throws Exception {
		SmartPersonA p = newSmartPersonA();
		p.setNameA("p1");

		Company c1 = newCompany();
		c1.setNameA("c1");

		Company c2 = newCompany();
		c2.setNameA("c2");

		// here we set the key-property value to our preliminary instance
		p.setKeyCompanySetA(new HashSet<Company>());
		p.getKeyCompanySetA().add(c1);
		p.getKeyCompanySetA().add(c2);

		commit();

		PersonA personA = personAByName("p1");
		BtAssertions.assertThat(personA.getCompanyNameSetA()).containsOnly("c1", "c2");
	}

	@Test
	public void bulkInsertToCollection_BothPreliminary() throws Exception {
		SmartPersonA p = newSmartPersonA();
		p.setNameA("p1");

		Company c1 = newCompany();
		c1.setNameA("c1");

		Company c2 = newCompany();
		c2.setNameA("c2");

		// here we set the key-property value to our preliminary instance
		p.setKeyCompanySetA(new HashSet<Company>());
		p.getKeyCompanySetA().addAll(Arrays.asList(c1, c2));

		commit();

		PersonA personA = personAByName("p1");
		BtAssertions.assertThat(personA.getCompanyNameSetA()).containsOnly("c1", "c2");
	}

	@Test
	public void removeFromCollection() throws Exception {
		SmartPersonA p = newSmartPersonA();
		p.setNameA("p1");

		Company c1 = newCompany();
		c1.setNameA("c1");

		Company c2 = newCompany();
		c2.setNameA("c2");

		// here we set the key-property value to our preliminary instance
		p.setKeyCompanySetA(new HashSet<Company>());
		p.getKeyCompanySetA().addAll(Arrays.asList(c1, c2));
		commit();

		p.getKeyCompanySetA().remove(c2);
		commit();

		PersonA personA = personAByName("p1");
		BtAssertions.assertThat(personA.getCompanyNameSetA()).containsOnly("c1");
	}

	@Test
	public void bulkRemoveFromCollection() throws Exception {
		SmartPersonA p = newSmartPersonA();
		p.setNameA("p1");

		Company c1 = newCompany();
		c1.setNameA("c1");

		Company c2 = newCompany();
		c2.setNameA("c2");

		// here we set the key-property value to our preliminary instance
		p.setKeyCompanySetA(new HashSet<Company>());
		p.getKeyCompanySetA().addAll(Arrays.asList(c1, c2));
		commit();

		p.getKeyCompanySetA().removeAll(Arrays.asList(c1, c2));
		commit();

		PersonA personA = personAByName("p1");
		BtAssertions.assertThat(personA.getCompanyNameSetA()).isEmpty();
	}

}
