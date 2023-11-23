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
package com.braintribe.model.processing.smood.manipulation;

import static com.braintribe.model.processing.test.tools.meta.ManipulationTrackingMode.GLOBAL;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.generic.manipulation.CompoundManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.session.api.notifying.NotifyingGmSession;

/**
 * 
 */
public class GlobalManipulationTests extends AbstractSmoodManipulationTests {

	private boolean adjust;

	@Before
	public void setManipulationMode() {
		defaultManipulationMode = GLOBAL;

		adjust = false;
		applyManipulations(this::createPerson);
		adjust = true;
	}

	@Test
	public void createEntityAndSetId() {
		applyManipulations(session -> {
			Person p = createPerson(session);
			p.setName("p1");
		});

	}

	private Person createPerson(NotifyingGmSession session) {
		Person p = session.create(Person.T);
		p.setGlobalId("person1");

		return p;
	}

	@Override
	protected ManipulationRequest asRequest(Manipulation m) {
		if (adjust) {
			CompoundManipulation cm = (CompoundManipulation) m;
			cm.getCompoundManipulationList().remove(0); // instantiation
			cm.getCompoundManipulationList().remove(0); // set globalId to "p1"
		}

		return super.asRequest(m);
	}

}
