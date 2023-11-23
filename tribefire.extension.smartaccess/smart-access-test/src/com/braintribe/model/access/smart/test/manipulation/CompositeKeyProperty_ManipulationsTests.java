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

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.CompositeKpaEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class CompositeKeyProperty_ManipulationsTests extends AbstractManipulationsTests {

	@Test
	public void changeEntityValue_BothPreliminary() throws Exception {
		SmartPersonA p = newSmartPersonA();
		p.setNameA("p1");

		CompositeKpaEntity c = newCompositeKpaEntity();
		c.setPersonId(99L);
		c.setPersonName("pp1");
		c.setPersonCompanyName("c1");

		// here we set the composite-key-property value to our preliminary instance
		p.setCompositeKpaEntity(c);

		commit();

		PersonA personA = personAByName("p1");
		BtAssertions.assertThat(personA.getCompositeId()).isEqualTo(99L);
		BtAssertions.assertThat(personA.getCompositeName()).isEqualTo("pp1");
		BtAssertions.assertThat(personA.getCompositeCompanyName()).isEqualTo("c1");
	}

	@Test
	public void changeEntityValue_BothPersistent() throws Exception {
		SmartPersonA p = newSmartPersonA();
		p.setNameA("p1");

		CompositeKpaEntity c = newCompositeKpaEntity();
		c.setPersonId(99L);
		c.setPersonName("pp1");
		c.setPersonCompanyName("c1");

		commit();

		// here we set the composite-key-property value to our preliminary instance
		p.setCompositeKpaEntity(c);

		commit();

		PersonA personA = personAByName("p1");
		BtAssertions.assertThat(personA.getCompositeId()).isEqualTo(99L);
		BtAssertions.assertThat(personA.getCompositeName()).isEqualTo("pp1");
		BtAssertions.assertThat(personA.getCompositeCompanyName()).isEqualTo("c1");
	}

}
