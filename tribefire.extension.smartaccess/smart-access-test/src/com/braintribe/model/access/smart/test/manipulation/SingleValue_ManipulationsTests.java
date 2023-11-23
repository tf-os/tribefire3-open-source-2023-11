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
import com.braintribe.model.processing.query.smart.test.model.accessB.StandardIdEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.Company;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartStringIdEntity;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class SingleValue_ManipulationsTests extends AbstractManipulationsTests {

	@Test
	public void instantiation() throws Exception {
		SmartPersonA p = newSmartPersonA();
		commit();

		BtAssertions.assertThat(p.<Object> getId()).isNotNull();
		BtAssertions.assertThat(countPersonA()).isEqualTo(1);
	}

	@Test
	public void instantiation_WithConvertedId() throws Exception {
		SmartStringIdEntity s = newStandardStringIdEntity();
		commit();

		BtAssertions.assertThat(s.<Object> getId()).isNotNull();
		BtAssertions.assertThat(count(StandardIdEntity.class, smoodB)).isEqualTo(1);
	}

	/* There was a bug, that id (set by induced manipulation) was only set to the first entity */
	@Test
	public void instantiation_Multiple() throws Exception {
		SmartPersonA p1 = newSmartPersonA();
		SmartPersonA p2 = newSmartPersonA();
		commit();

		BtAssertions.assertThat(p1.<Object> getId()).isNotNull();
		BtAssertions.assertThat(p2.<Object> getId()).isNotNull();
		BtAssertions.assertThat(countPersonA()).isEqualTo(2);
	}

	@Test
	public void changeSimpleValue() throws Exception {
		SmartPersonA p = newSmartPersonA();
		commit();

		p.setNameA("p1");
		commit();

		BtAssertions.assertThat(personAByName("p1")).isNotNull();
	}

	@Test
	public void changeSimpleValue_WithConvertedId() throws Exception {
		SmartStringIdEntity s = newStandardStringIdEntity();
		commit();

		s.setName("s");
		commit();

		BtAssertions.assertThat(standardIdEntityByName("s")).isNotNull();
	}

	@Test
	public void changeSimpleValue_PartitionInference() throws Exception {
		SmartPersonA p = newSmartPersonA();
		commit();

		session.suspendHistory();
		p.setPartition("*");
		session.resumeHistory();
		
		p.setNameA("p1");
		commit();

		BtAssertions.assertThat(personAByName("p1")).isNotNull();
	}

	@Test
	public void changeEntityValue_Preliminary() throws Exception {
		SmartPersonA p = newSmartPersonA();
		p.setNameA("p");
		commit();

		Company c = newCompany();
		c.setNameA("braintribe");

		p.setCompanyA(c);
		commit();

		PersonA pA = personAByName("p");
		BtAssertions.assertThat(pA).isNotNull();
		BtAssertions.assertThat(pA.getCompanyA()).isNotNull();
		BtAssertions.assertThat(pA.getCompanyA().getNameA()).isEqualTo("braintribe");
	}

	@Test
	public void changeEntityValue_Persistent() throws Exception {
		SmartPersonA p = newSmartPersonA();
		p.setNameA("p");
		commit();

		Company c = newCompany();
		c.setNameA("braintribe");
		commit();

		p.setCompanyA(c);
		commit();

		PersonA pA = personAByName("p");
		BtAssertions.assertThat(pA).isNotNull();
		BtAssertions.assertThat(pA.getCompanyA()).isNotNull();
		BtAssertions.assertThat(pA.getCompanyA().getNameA()).isEqualTo("braintribe");
	}

	@Test
	public void changeEntityValue_Null() throws Exception {
		SmartPersonA p = newSmartPersonA();
		p.setNameA("p");
		commit();

		p.setCompanyA(null);
		commit();

		PersonA pA = personAByName("p");
		BtAssertions.assertThat(pA).isNotNull();
		BtAssertions.assertThat(pA.getCompanyA()).isNull();
	}

	@Test
	public void delete() throws Exception {
		SmartPersonA p = newSmartPersonA();
		commit();

		session.deleteEntity(p);
		commit();

		BtAssertions.assertThat(countPersonA()).isEqualTo(0);
	}

}
