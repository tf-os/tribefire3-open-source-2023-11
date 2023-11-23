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

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessB.PersonB;
import com.braintribe.model.processing.query.smart.test.model.accessB.StandardIdEntity;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonB;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartStringIdEntity;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class ConvertedProperties_ManipulationsTests extends AbstractManipulationsTests {

	private SmartPersonB sp;

	@Before
	public void prepareData() throws Exception {
		sp = newSmartPersonB();
		sp.setNameB("sp");
		commit();

		BtAssertions.assertThat(countPersonB()).isEqualTo(1);
	}

	// ####################################
	// ## . . . . Single value . . . . . ##
	// ####################################

	@Test
	public void setSingleValue() throws Exception {
		sp.setConvertedBirthDate(new Date(0));
		commit();

		PersonB p = loadDelegate();
		BtAssertions.assertThat(p.getBirthDate()).isNotEmpty();
	}

	// ####################################
	// ## . . . . Collection value . . . ##
	// ####################################

	@Test
	public void setCollectionValue() throws Exception {
		sp.setConvertedDates(asList(new Date(0), new Date(1000)));
		commit();

		PersonB p = loadDelegate();
		BtAssertions.assertThat(p.getDates()).isNotEmpty().hasSize(2);
	}

	@Test
	public void insertToCollectionValue() throws Exception {
		sp.setConvertedDates(new ArrayList<Date>());
		sp.getConvertedDates().add(new Date(0));
		commit();

		PersonB p = loadDelegate();
		BtAssertions.assertThat(p.getDates()).isNotEmpty().hasSize(1);
	}

	@Test
	public void bulkInsertToCollectionValue() throws Exception {
		sp.setConvertedDates(new ArrayList<Date>());
		sp.getConvertedDates().addAll(asList(new Date(0), new Date(1000)));
		commit();

		PersonB p = loadDelegate();
		BtAssertions.assertThat(p.getDates()).isNotEmpty().hasSize(2);
	}

	@Test
	public void removeFromCollectionValue() throws Exception {
		insertToCollectionValue();

		sp.getConvertedDates().remove(0);
		commit();

		PersonB p = loadDelegate();
		BtAssertions.assertThat(p.getDates()).isEmpty();
	}

	@Test
	public void bulkRemoveFromCollectionValue() throws Exception {
		insertToCollectionValue();

		sp.getConvertedDates().removeAll(asList(new Date(0), new Date(1000)));
		commit();

		PersonB p = loadDelegate();
		BtAssertions.assertThat(p.getDates()).isEmpty();
	}

	// ####################################
	// ## . . . . . . Id value . . . . . ##
	// ####################################

	@Test
	public void instantiateEntityWithConvertedId() throws Exception {
		newEntity(SmartStringIdEntity.T);
		commit();

		BtAssertions.assertThat(count(StandardIdEntity.class, smoodB)).isEqualTo(1);
	}

	private PersonB loadDelegate() {
		return selectByProperty(PersonB.class, "nameB", "sp", smoodB);
	}

}
