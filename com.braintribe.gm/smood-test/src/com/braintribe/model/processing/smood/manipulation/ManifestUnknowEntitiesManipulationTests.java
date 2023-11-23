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

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;

import com.braintribe.model.accessapi.ManipulationRequest;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.Manipulation;
import com.braintribe.model.generic.reflection.TraversingContext;
import com.braintribe.model.generic.reflection.TraversingVisitor;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.processing.query.test.model.Owner;
import com.braintribe.model.processing.query.test.model.Person;
import com.braintribe.model.processing.test.tools.meta.ManipulationTrackingMode;

/**
 * 
 */
public class ManifestUnknowEntitiesManipulationTests extends AbstractSmoodManipulationTests {

	/**
	 * When recording the manipulations, the {@link ChangeValueManipulation} for the name will be referenced using a
	 * {@link PersistentEntityReference}, because we have set an id in the previous step. Thus the process inside the
	 * {@link #asRequest} method will cause our stack to have a non-resolvable reference, which should be manifested.
	 */
	@Test
	public void createEntityForUnknownReference() {
		applyManipulations(ManipulationTrackingMode.PERSISTENT, session -> {
			Person p = session.create(Person.T);
			p.setId(1L);
			p.setName("p2");
		});

		assertEntityCountForType(Owner.T, 0);
		assertEntityCountForType(Person.T, 2);
		assertEntityCountForType(GenericEntity.T, 2);
	}

	@Override
	protected ManipulationRequest asRequest(Manipulation m) {
		ManipulationRequest mr = super.asRequest(m);

		for (PersistentEntityReference ref : findReferences(mr.getManipulation())) {
			ref.setRefId((Long) ref.getRefId() + 100);
		}

		return mr;
	}

	@Override
	protected boolean manifestUnknownEntities() {
		return true;
	}

	private static Set<PersistentEntityReference> findReferences(Manipulation m) {
		return m.touchedEntities() //
				.filter(e -> e instanceof PersistentEntityReference) //
				.map(e -> (PersistentEntityReference) e) //
				.collect(Collectors.toSet());
	}

	static class ReferenceCollectingVisitor implements TraversingVisitor {
		Set<PersistentEntityReference> refs = newSet();

		@Override
		public void visitTraversing(TraversingContext tc) {
			Object o = tc.getObjectStack().peek();

			if (o instanceof PersistentEntityReference)
				refs.add((PersistentEntityReference) o);
		}
	}

}
