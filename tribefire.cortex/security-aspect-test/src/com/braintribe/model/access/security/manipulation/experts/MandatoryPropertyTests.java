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

import com.braintribe.model.access.security.SecurityAspect;
import com.braintribe.model.access.security.manipulation.ValidatorTestBase;
import com.braintribe.model.access.security.testdata.manipulation.EntityWithPropertyConstraints;
import com.braintribe.model.processing.security.manipulation.ManipulationSecurityExpert;

/**
 * 
 */
public class MandatoryPropertyTests extends ValidatorTestBase {

	@Override
	protected Set<? extends ManipulationSecurityExpert> manipulationSecurityExperts() {
		return asSet(new MandatoryPropertyExpert());
	}

	@Test
	public void settingPropertyIsOk() throws Exception {
		validate(() -> {
			EntityWithPropertyConstraints entity = session.create(EntityWithPropertyConstraints.T);
			entity.setMandatory("value");
			entity.setNonModifiableButMandatory("value");
		});

		assertOk();
	}

	/** There was a bug that this did not work due to {@link SecurityAspect}s wrong handling of changing id property. */
	@Test
	public void settingPropertyIsOk_WhenChangingId() throws Exception {
		validate(() -> {
			EntityWithPropertyConstraints entity = session.create(EntityWithPropertyConstraints.T);
			entity.setId(10);
			entity.setMandatory("value");
			entity.setNonModifiableButMandatory("value");
		});

		assertOk();
	}

	@Test
	public void notSettingPropertyCausesFailure() throws Exception {
		validate(() -> session.create(EntityWithPropertyConstraints.T));

		assertNumberOfErrors(2);
	}

	@Test
	public void settingAndUnsettingPropertyCausesFailure() throws Exception {
		validate(() -> {
			EntityWithPropertyConstraints entity = session.create(EntityWithPropertyConstraints.T);
			entity.setNonModifiableButMandatory("value");
			entity.setMandatory("value");
			entity.setMandatory(null);
		});

		assertSingleError();
	}

	@Test
	public void unsettingPropertyCausesFailure() throws Exception {
		EntityWithPropertyConstraints entity = session.create(EntityWithPropertyConstraints.T);
		entity.setNonModifiableButMandatory("value");
		entity.setMandatory("value");

		validate(() -> entity.setMandatory(null));

		assertSingleError();
	}

	private void assertSingleError() {
		assertNumberOfErrors(1);
		assertErrors(EntityWithPropertyConstraints.T, "mandatory");
	}

}
