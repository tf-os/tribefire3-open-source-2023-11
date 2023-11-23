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
package com.braintribe.model.access.sql.manipulation;

import static com.braintribe.utils.lcd.CollectionTools2.newSet;

import java.util.List;
import java.util.Set;

import com.braintribe.cc.lcd.CodingSet;
import com.braintribe.model.generic.GenericEntity;
import com.braintribe.model.generic.commons.EntRefHashingComparator;
import com.braintribe.model.generic.manipulation.AtomicManipulation;
import com.braintribe.model.generic.manipulation.ChangeValueManipulation;
import com.braintribe.model.generic.manipulation.ManipulationType;
import com.braintribe.model.generic.tools.AssemblyTools;
import com.braintribe.model.generic.value.PersistentEntityReference;
import com.braintribe.model.generic.value.PreliminaryEntityReference;

/**
 * Here we scan the manipulations and look for all the relevant {@link PreliminaryEntityReference}s.
 * 
 * Yes, there are some which are not relevant. In case the user assigns an id himself, we ignore all the (subsequent)
 * references related to this id. This is correct because this must be an id that was not used before (we say id's are
 * unique globally, but maybe we should check?) hence we really can't load an entity with such id.
 * 
 * @author peter.gazdik
 */
public class PersistentReferenceScanner {

	public static Set<PersistentEntityReference> findPersistentReferences(List<AtomicManipulation> manipulations) {
		return new PersistentReferenceScanner(manipulations).findPersistentReferences();
	}

	private final List<AtomicManipulation> manipulations;
	private final Set<String> explicitlySetIds = newSet();
	private final Set<PersistentEntityReference> result = CodingSet.create(EntRefHashingComparator.INSTANCE);

	public PersistentReferenceScanner(List<AtomicManipulation> manipulations) {
		this.manipulations = manipulations;
	}

	private Set<PersistentEntityReference> findPersistentReferences() {
		for (AtomicManipulation m : manipulations) {
			if (m.manipulationType() == ManipulationType.CHANGE_VALUE)
				if (handleIfIdAssignment((ChangeValueManipulation) m))
					continue; //

			handlePersistentReferencesIn(m);
		}

		return result;

	}

	private void handlePersistentReferencesIn(AtomicManipulation m) {
		Set<PersistentEntityReference> references = AssemblyTools.findAll(m, PersistentEntityReference.T,
				p -> !explicitlySetIds.contains(p.getRefId()));

		result.addAll(references);
	}

	/** @return true iff this {@link ChangeValueManipulation} is an id assignment. */
	private boolean handleIfIdAssignment(ChangeValueManipulation cvm) {
		if (!GenericEntity.id.equals(cvm.getOwner().getPropertyName()))
			return false;

		Object newId = cvm.getNewValue();
		explicitlySetIds.add((String) newId);
		return true;
	}

}
