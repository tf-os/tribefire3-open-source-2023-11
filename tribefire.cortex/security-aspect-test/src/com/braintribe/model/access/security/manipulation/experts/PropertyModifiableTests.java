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
package com.braintribe.model.access.security.manipulation.experts;

import static com.braintribe.utils.lcd.CollectionTools2.asSet;

import java.util.Set;

import org.junit.Test;

import com.braintribe.model.access.security.manipulation.ValidatorTestBase;
import com.braintribe.model.access.security.testdata.manipulation.EntityWithPropertyConstraints;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpert;

/**
 * 
 */
public class PropertyModifiableTests extends ValidatorTestBase {

	@Override
	protected Set<? extends ManipulationSecurityExpert> manipulationSecurityExperts() {
		return asSet(new PropertyModifiableExpert());
	}

	@Test
	public void noContraint_EditOk() throws Exception {
		validate(() -> {
			EntityWithPropertyConstraints entity = session.create(EntityWithPropertyConstraints.T);
			entity.setUnique("value");
		});

		assertOk();
	}

	@Test
	public void nonModifiable_Error() throws Exception {
		validate(() -> {
			EntityWithPropertyConstraints entity = session.create(EntityWithPropertyConstraints.T);
			entity.setNonModifiable("value");
		});

		assertSingleError("nonModifiable");
	}

	@Test
	public void nonModifiableButMandatory_Instantiation_Ok() throws Exception {
		validate(() -> {
			EntityWithPropertyConstraints entity = session.create(EntityWithPropertyConstraints.T);
			entity.setNonModifiableButMandatory("value");
		});
		
		assertOk();
	}
	
	@Test
	public void nonModifiableButMandatory_Persistent_Ok() throws Exception {
		EntityWithPropertyConstraints entity = session.create(EntityWithPropertyConstraints.T);
		commit();

		validate(() -> {
			entity.setNonModifiableButMandatory("value");
		});
		
		assertSingleError("nonModifiableButMandatory");
	}

	// @Test
	public void KNOWN_ISSUE_nonModifiableButMandatory_InstantiationAfterExplicitIdSet_Ok() throws Exception {
		validate(() -> {
			EntityWithPropertyConstraints entity = session.create(EntityWithPropertyConstraints.T);
			entity.setId(1L);
			entity.setNonModifiableButMandatory("value");
		});
		
		assertOk();
	}
	
	private void assertSingleError(String propertyName) {
		assertNumberOfErrors(1);
		assertErrors(EntityWithPropertyConstraints.T, propertyName);
	}

}
