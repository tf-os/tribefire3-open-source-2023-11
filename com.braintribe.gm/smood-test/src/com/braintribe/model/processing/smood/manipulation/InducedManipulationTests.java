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

import java.util.Objects;

import org.junit.Before;
import org.junit.Test;

import com.braintribe.common.lcd.EmptyReadWriteLock;
import com.braintribe.model.generic.value.PreliminaryEntityReference;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.smood.Smood;
import com.braintribe.model.processing.test.tools.meta.ManipulationTrackingMode;

/**
 * 
 */
public class InducedManipulationTests extends AbstractSmoodManipulationTests {

	@Before
	public void setManipulationMode() {
		defaultManipulationMode = ManipulationTrackingMode.PERSISTENT;
	}

	Person p;

	/**
	 * In this test we simply track an instantiation manipulation and remember the created entity. Then, we go on to
	 * attach this entity to a new Smood and on that we apply the induced manipulation.
	 * 
	 * NOTE it is important to use the original entity because the induced manipulation uses a
	 * {@link PreliminaryEntityReference} that is only valid for that concrete instance.
	 */
	@Test
	public void checkInducedManipulationIsCorrect() throws Exception {
		applyManipulations(session -> {
			p = session.create(Person.T);
		});

		Objects.isNull(p.getId());
		Objects.isNull(p.getGlobalId());

		Smood smood = new Smood(EmptyReadWriteLock.INSTANCE);
		smood.setMetaModel(provideEnrichedMetaModel());
		smood.registerEntity(p, false);

		smood.apply().generateId(false).localRequest(false).request2(asRequest(response.getInducedManipulation()));

		Objects.nonNull(p.getId());
		Objects.nonNull(p.getGlobalId());
	}

	@Test
	public void checkInducedManipulationIsCorrect_WhenPersistenceIdAssignedManuall() throws Exception {
		applyManipulations(session -> {
			p = session.create(Person.T);
			p.setId("p1");
		});

		Objects.nonNull(p.getId());
		Objects.isNull(p.getGlobalId());

		Smood smood = new Smood(EmptyReadWriteLock.INSTANCE);
		smood.setMetaModel(provideEnrichedMetaModel());
		smood.registerEntity(p, false);

		smood.apply().generateId(false).localRequest(false).request2(asRequest(response.getInducedManipulation()));

		Objects.nonNull(p.getId());
		Objects.nonNull(p.getGlobalId());
	}
}
