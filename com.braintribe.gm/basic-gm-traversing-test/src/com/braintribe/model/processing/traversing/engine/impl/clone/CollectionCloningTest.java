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
package com.braintribe.model.processing.traversing.engine.impl.clone;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.processing.traversing.engine.GMT;
import com.braintribe.model.processing.traversing.engine.impl.misc.model.ListOwner;
import com.braintribe.testing.tools.gm.meta.ManipulationRecorder;

public class CollectionCloningTest {

	@Test
	public void collectionManipulationsAreTracked() throws Exception {
		ManipulationRecorder manipulationRecorder = new ManipulationRecorder();

		ListOwner listOwner = ListOwner.T.create();
		listOwner.setList(Arrays.asList(ListOwner.T.create()));

		manipulationRecorder.record(session -> {
			BasicClonerCustomization clonerCustomization = new BasicClonerCustomization();
			clonerCustomization.setSession(session);

			Cloner cloner = new Cloner();
			GMT.doClone().customize(clonerCustomization).visitor(cloner).doFor(listOwner);

			assertContainsAddManipulation(session.getTransaction().getManipulationsDone());
		});
	}

	private void assertContainsAddManipulation(List<Manipulation> manipulations) {
		Optional<?> add = manipulations.stream().filter(m -> m.manipulationType() == ManipulationType.ADD).findFirst();
		assertThat(add.isPresent()).as("Add manipulation not tracked.").isTrue();
	}

}
