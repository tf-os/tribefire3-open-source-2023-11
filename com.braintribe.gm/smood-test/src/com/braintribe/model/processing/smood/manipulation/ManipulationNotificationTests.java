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

import static com.braintribe.model.processing.test.tools.meta.ManipulationTrackingMode.LOCAL;
import static com.braintribe.model.processing.test.tools.meta.ManipulationTrackingMode.PERSISTENT;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.InstantiationManipulation;
import com.braintribe.model.generic.manipulation.ManifestationManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.EntityType;
import com.braintribe.model.generic.tracking.ManipulationListener;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.utils.junit.assertions.BtAssertions;

/**
 * 
 */
public class ManipulationNotificationTests extends AbstractSmoodManipulationTests {

	private final List<Manipulation> manipulations = new ArrayList<Manipulation>();

	// TODO CHECK: do we want manifestations here?
	@Test
	public void createEntity_Local() {
		applyManipulations(LOCAL, session -> {
			smood.getGmSession().listeners().add(new Listener());

			session.create(Person.T);
		});

		assertAllManipulationTypes(ManifestationManipulation.T, ChangeValueManipulation.T, ChangeValueManipulation.T);
	}

	@Test
	public void createEntity_IdNotSet() {
		this.generateId = false;

		applyManipulations(LOCAL, session -> {
			smood.getGmSession().listeners().add(new Listener());

			session.create(Person.T);
		});

		assertAllManipulationTypes(ManifestationManipulation.T);
	}

	@Test
	public void createEntity_Remote() {
		applyManipulations(PERSISTENT, session -> {
			smood.getGmSession().listeners().add(new Listener());

			session.create(Person.T);
		});

		assertAllManipulationTypes(InstantiationManipulation.T, ChangeValueManipulation.T, ChangeValueManipulation.T);
	}

	private void assertAllManipulationTypes(EntityType<? extends Manipulation>... ms) {
		BtAssertions.assertThat(manipulations).hasSize(ms.length);

		int counter = 0;
		for (Manipulation tracked : manipulations) {
			EntityType<? extends Manipulation> expectedType = ms[counter++];

			BtAssertions.assertThat(tracked.entityType()).isSameAs(expectedType);
		}
	}

	private class Listener implements ManipulationListener {
		@Override
		public void noticeManipulation(Manipulation manipulation) {
			manipulations.add(manipulation);
		}

	}

}
