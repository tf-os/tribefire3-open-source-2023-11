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

import static com.braintribe.model.processing.query.smart.test.setup.base.SmartMappingSetup.accessIdA;

import org.junit.Test;

import com.braintribe.model.processing.query.smart.test.model.accessA.PersonA;
import com.braintribe.model.processing.query.smart.test.model.smart.SmartPersonA;
import com.braintribe.model.processing.smart.SmartAccessException;
import com.braintribe.utils.junit.assertions.BtAssertions;

public class IdChanging_ManipulationsTests extends AbstractManipulationsTests {

	@Test
	public void manuallySetId() throws Exception {
		SmartPersonA sp = newSmartPersonA();
		sp.setId(10L);
		sp.setNameA("p");
		commit();

		PersonA pA = personAByName("p");

		BtAssertions.assertThat(pA.<Object> getId()).isEqualTo(10l);
		BtAssertions.assertThat(countPersonA()).isEqualTo(1);

		// Test automatically assigned partition is correct
		BtAssertions.assertThat(pA.getPartition()).isNull(); // we do not set it, so in the smood it is null
		BtAssertions.assertThat(sp.getPartition()).isEqualTo(accessIdA); // automatically assigned will be accessA
	}

	/**
	 * There was a bug where setting id to null would lead to an NPE. This was fixed by ignoring such manipulation in
	 * the ReferenceManager when the original id was also null.
	 * 
	 * Note that setting id null to an entity with id would lead to a {@link SmartAccessException}.
	 */
	@Test
	public void setIdToNull_Preliminary() throws Exception {
		SmartPersonA sp = newSmartPersonA();
		sp.setId(null);
		sp.setNameA("p");
		commit();

		PersonA pA = personAByName("p");

		BtAssertions.assertThat(pA.<Object> getId()).isNotNull();
		BtAssertions.assertThat(countPersonA()).isEqualTo(1);

		// Test automatically assigned partition is correct
		BtAssertions.assertThat(pA.getPartition()).isNull(); // we do not set it, so in the smood it is null
		BtAssertions.assertThat(sp.getPartition()).isEqualTo(accessIdA); // automatically assigned will be accessA
	}

	/** @see #setIdToNull_Preliminary() */
	@Test(expected=RuntimeException.class)
	public void setIdToNull_Persistent() throws Exception {
		SmartPersonA sp = newSmartPersonA();
		sp.setNameA("p");
		commit();

		sp.setId(null);
		commit();
	}
}
